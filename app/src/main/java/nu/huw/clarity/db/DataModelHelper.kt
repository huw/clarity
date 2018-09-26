package nu.huw.clarity.db

import android.support.annotation.WorkerThread
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.R
import nu.huw.clarity.db.dao.ContextDao
import nu.huw.clarity.db.dao.FolderDao
import nu.huw.clarity.db.dao.PerspectiveDao
import nu.huw.clarity.db.dao.TaskDao
import nu.huw.clarity.model.*
import org.threeten.bp.Duration
import java.util.*
import javax.inject.Inject
import kotlin.Comparator

class DataModelHelper(androidContext: android.content.Context) {

    @Inject lateinit var db: AppDatabase
    @Inject lateinit var app: android.content.Context

    init {
        (androidContext.applicationContext as ClarityApplication).component.inject(this)
    }

    /**
     * Logic for using perspectives to get entries
     *
     * @param perspective The perspective used for filtering/sorting entries
     * @param parent The parent entry (nullable) to determine what to retrieve
     * @return A TreeMap with Header keys and TreeSet values, containing Entries. The headers help split up the list into groups, while each of the lists is ordered depending on the perspective's settings.
     */
    @WorkerThread
    fun getEntriesFromPerspective(perspective: Perspective, parent: Entry?): TreeMap<Header, SortedSet<out Entry>> = when (perspective.id.value) {
        "ProcessContexts" -> getFromContexts(perspective, parent)
        "ProcessInbox" -> getFromInbox(perspective)
        "ProcessProjects" -> getFromProjects(perspective, parent)
        // "ProcessFlagged", "ProcessForecast",
        else -> getFromTasks(perspective, parent as Task)
    }

    /**
     * Gets a list of entries in the Contexts perspective, in half the lines as Java
     */
    fun getFromContexts(perspective: Perspective, parent: Entry?): TreeMap<Header, SortedSet<out Entry>> {

        // If the parent is a project or task, it can only have task children
        if (parent is Task) return getFromTasks(perspective, parent)

        // Get items
        val contexts = db.contextDao()
                .getFromParent(parent as Context)
                .toSortedSet()

        val tasks = db.taskDao()
                .getFromContext(parent)
                .filter { filterStatus(perspective, it) }
                .filter { filterFlagged(perspective, it) }
                .filter { filterDuration(perspective, it) }
                .toSortedSet(getTaskComparator(perspective))

        // Terse syntax, but creates an outer set sorted by entryType, then adds our lists with headers
        return TreeMap<Header, SortedSet<out Entry>>(compareBy<Header> { it.entryType }).apply {
            put(Header(app.getString(R.string.listitem_headingcontexts), entryType = EntryTypeState.CONTEXT), contexts)
            put(Header(app.getString(R.string.listitem_headingtasks), entryType = EntryTypeState.TASK), tasks)
        }
    }

    /**
     * Gets a list of entries in the inbox perspective
     */
    fun getFromInbox(perspective: Perspective): TreeMap<Header, SortedSet<out Entry>> {

        val tasks = db.taskDao()
                .getInbox()
                .filter { filterStatus(perspective, it) }
                .filter { filterFlagged(perspective, it) }
                .filter { filterDuration(perspective, it) }
                .toSortedSet(getTaskComparator(perspective))

        return TreeMap<Header, SortedSet<out Entry>>().apply {
            put(Header(app.getString(R.string.listitem_headingitems)), tasks)
        }
    }

    /**
     * Gets a list of entries in the projects perspective
     */
    fun getFromProjects(perspective: Perspective, parent: Entry?): TreeMap<Header, SortedSet<out Entry>> {

        // If the parent is a project or task, it can only have task children
        if (parent is Task) return getFromTasks(perspective, parent)

        // Get items
        val folders = db.folderDao().getFromParent(parent as Folder)
        val tasks = db.taskDao()
                .getProjectsFromParent(parent)
                .filter { filterStatus(perspective, it) }
                .filter { filterFlagged(perspective, it) }
                .filter { filterDuration(perspective, it) }

        return TreeMap<Header, SortedSet<out Entry>>().apply {
            put(Header(app.getString(R.string.listitem_headingitems)), folders.union(tasks).toSortedSet())
        }
    }

    /**
     * Gets a list of entries in almost any other perspective
     */
    fun getFromTasks(perspective: Perspective, parent: Task?): TreeMap<Header, SortedSet<out Entry>> {

        val tasks = db.taskDao()
                .getTasksFromParentOrAll(parent)
                .filter { filterStatus(perspective, it) }
                .filter { filterFlagged(perspective, it) }
                .filter { filterDuration(perspective, it) }
                .toSortedSet(getTaskComparator(perspective))

        // TODO: Split tasks into TreeMap

        return TreeMap<Header, SortedSet<out Entry>>().apply {
            put(Header(app.getString(R.string.listitem_headingitems)), tasks)
        }
    }

    /**
     * Useful for filter chains. Filters by status.
     */
    fun filterStatus(perspective: Perspective, task: Task) = when (perspective.filterStatus) {
        StatusFilterState.NONE -> true
        StatusFilterState.COMPLETED -> task.completed
        StatusFilterState.REMAINING -> task.remaining
        StatusFilterState.AVAILABLE,
        StatusFilterState.FIRST_AVAILABLE -> task.available
    }

    /**
     * Filters by flagged & due state
     */
    fun filterFlagged(perspective: Perspective, task: Task) = when (perspective.filterFlagged) {
        FlaggedFilterState.NONE -> true
        FlaggedFilterState.FLAGGED -> task.flagged
        FlaggedFilterState.UNFLAGGED -> !task.flagged
        FlaggedFilterState.DUE -> task.dueSoon || task.overdue
        FlaggedFilterState.DUE_OR_FLAGGED -> task.flagged || task.dueSoon || task.overdue
        FlaggedFilterState.DUE_AND_FLAGGED -> task.flagged && (task.dueSoon || task.overdue)
        FlaggedFilterState.DUE_AND_UNFLAGGED -> !task.flagged && (task.dueSoon || task.overdue)
    }

    /**
     * Filters by duration
     * If the duration is null, then it should only pass when the filter is none or unestimated
     * Unfortunately, a simple comparison would yield the wrong result because of ascending orders
     */
    fun filterDuration(perspective: Perspective, task: Task): Boolean = when (perspective.filterDuration) {
        DurationFilterState.NONE -> true
        DurationFilterState.UNESTIMATED -> task.duration == null
        DurationFilterState.FIVE -> task.duration != null && Duration.ofMinutes(5) >= task.duration
        DurationFilterState.FIFTEEN -> task.duration != null && Duration.ofMinutes(15) >= task.duration
        DurationFilterState.THIRTY -> task.duration != null && Duration.ofMinutes(30) >= task.duration
        DurationFilterState.HOUR -> task.duration != null && Duration.ofHours(1) >= task.duration
        DurationFilterState.LONG -> task.duration != null && Duration.ofHours(1) < task.duration
    }

    /**
     * Funny, this clears up literally 500 lines of Java
     */
    fun getTaskComparator(perspective: Perspective?): Comparator<Task> = when (perspective?.sort) {
        SortState.CONTEXT -> compareBy<Task> { db.contextDao().getFromTask(it) }.thenBy { it.rank }
        SortState.PROJECT -> compareBy<Task> { db.taskDao().getProjectFromTask(it) }.thenBy { it.rank }
        SortState.DUE -> compareByDescending<Task> { it.dateDue }.thenBy { it.rank }
        SortState.DEFER -> compareByDescending<Task> { it.dateDefer }.thenBy { it.rank }
        SortState.COMPLETED -> compareBy<Task> { it.dateCompleted }.thenBy { it.rank }
        SortState.ADDED -> compareBy<Task> { it.dateAdded }.thenBy { it.rank }
        SortState.MODIFIED -> compareBy<Task> { it.dateModified }.thenBy { it.rank }
        SortState.FLAGGED -> compareBy<Task> { it.flaggedState }.thenBy { it.rank }
        SortState.DURATION -> compareByDescending<Task> { it.duration }.thenBy { it.rank }
        SortState.NONE, null -> compareBy<Task> { it.rank }
    }

    fun getHeaderComparator(perspective: Perspective?): Comparator<Header> = when (perspective?.collation) {
        CollationState.TYPE -> compareBy { it.entryType }
        CollationState.FOLDER -> compareBy { db.folderDao().getFromHeader(it) }
        CollationState.CONTEXT -> compareBy { db.contextDao().getFromHeader(it) }
        CollationState.PROJECT -> compareBy { db.taskDao().getProjectFromHeader(it) }
        CollationState.DUE -> compareByDescending { it.dateDue }
        CollationState.DEFER -> compareByDescending { it.dateDefer }
        CollationState.COMPLETED -> compareBy { it.dateCompleted }
        CollationState.ADDED -> compareBy { it.dateAdded }
        CollationState.MODIFIED -> compareBy { it.dateModified }
        CollationState.FLAGGED -> compareBy { it.flagged }
        CollationState.NONE, null -> compareBy { 0 }
    }
}

/* ALL EXTENSION METHODS FOR DAO CLASSES SHOULD GO BELOW */

/**
 * Controls flow between two queries depending on nullness
 */
fun ContextDao.getFromParent(parent: Context?): List<Context> = if (parent != null) getFromNonNullParent(parent) else getTopLevel()
fun FolderDao.getFromParent(parent: Folder?): List<Folder> = if (parent != null) getFromNonNullParent(parent) else getTopLevel()
fun TaskDao.getProjectsFromParent(parent: Folder?): List<Task> = if (parent != null) getProjectsFromNonNullParent(parent) else getTopLevelProjects()
fun TaskDao.getTasksFromParent(parent: Task?): List<Task> = if (parent != null) getTasksFromNonNullParent(parent) else getTopLevelTasks()
fun TaskDao.getTasksFromParentOrAll(parent: Task?): List<Task> = if (parent != null) getTasksFromNonNullParent(parent) else getAll()
fun TaskDao.getFromParent(parent: Entry?): List<Task> = when (parent) {
    is Task -> getTasksFromParent(parent)
    is Folder -> getProjectsFromParent(parent)
    else -> throw IllegalArgumentException("Parent must be Task or Folder")
}

/**
 * Get the forecast perspective with its settings
 */
fun PerspectiveDao.getForecast(): Perspective = Perspective(
        id = ID("ProcessForecast", allowAny = true),
        filterDuration = DurationFilterState.NONE,
        filterFlagged = FlaggedFilterState.NONE,
        filterStatus = StatusFilterState.REMAINING,
        sort = SortState.DUE,
        collation = CollationState.DUE,
        name = "Forecast",
        iconState = PerspectiveIconState.FORECAST,
        colorState = PerspectiveColorState.RED
)