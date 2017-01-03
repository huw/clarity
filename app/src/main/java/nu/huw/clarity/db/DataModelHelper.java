package nu.huw.clarity.db;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import nu.huw.clarity.R;
import nu.huw.clarity.db.DatabaseContract.Attachments;
import nu.huw.clarity.db.DatabaseContract.Base;
import nu.huw.clarity.db.DatabaseContract.Contexts;
import nu.huw.clarity.db.DatabaseContract.Entries;
import nu.huw.clarity.db.DatabaseContract.Folders;
import nu.huw.clarity.db.DatabaseContract.Perspectives;
import nu.huw.clarity.db.DatabaseContract.Tasks;
import nu.huw.clarity.model.Attachment;
import nu.huw.clarity.model.Context;
import nu.huw.clarity.model.Entry;
import nu.huw.clarity.model.Folder;
import nu.huw.clarity.model.Perspective;
import nu.huw.clarity.model.Task;

/**
 * Like DatabaseHelper, DataModelHelper contains a number of helper classes for interacting with
 * the database, but only those specific to interacting with the data model (for views). Getters
 * and setters are all welcome.
 */
public class DataModelHelper {

    private static final String TAG           = DataModelHelper.class.getSimpleName();
    // Entries selections
    private static       String AND           = " AND ";
    private static       String OR            = " OR ";
    private static       String ORDER_BY_RANK = " ORDER BY " + Entries.COLUMN_RANK;
    private static       String TOP_LEVEL     = Entries.COLUMN_PARENT_ID + " IS NULL";
    private static       String ACTIVE        =
            Entries.COLUMN_ACTIVE + " = 1 AND " + Entries.COLUMN_ACTIVE_EFFECTIVE + " = 1";
    // Task selections
    private static       String REMAINING     =
            Tasks.COLUMN_DATE_COMPLETED + " IS NULL AND " + Tasks.COLUMN_DROPPED + " = 0";
    private static       String IN_INBOX      = Tasks.COLUMN_INBOX + " = 1";
    private static       String IS_PROJECT    = Tasks.COLUMN_PROJECT + " = 1";
    private static       String AVAILABLE     =
            REMAINING + AND + Tasks.COLUMN_BLOCKED + " = 0" + AND +
            Tasks.COLUMN_BLOCKED_BY_DEFER.name + " = 0";
    private static       String NO_CONTEXT       = Tasks.COLUMN_CONTEXT + " IS NULL";
    private static       String DUE_SOON         = Tasks.COLUMN_DUE_SOON + " = 1";
    private static       String OVERDUE          = Tasks.COLUMN_OVERDUE + " = 1";
    private static       String HAS_DUE          =
            "(" + Tasks.COLUMN_DATE_DUE + " IS NOT NULL" + OR + Tasks.COLUMN_DATE_DUE_EFFECTIVE +
            " IS NOT NULL)";
    private static       String HAS_DEFER        =
            "(" + Tasks.COLUMN_DATE_DEFER + " IS NOT NULL" + OR +
            Tasks.COLUMN_DATE_DEFER_EFFECTIVE + " IS NOT NULL)";
    private static       String HAS_DUE_OR_DEFER = "(" + HAS_DUE + OR + HAS_DEFER + ")";

    private DatabaseHelper          dbHelper;
    private android.content.Context mContext;
    private Random                  random;

    public DataModelHelper(android.content.Context context) {

        dbHelper = new DatabaseHelper(context);
        mContext = context;
        random = new Random();
    }

    /**
     * Given the ID of any entry, get it into the data model
     */
    public Entry getEntryFromID(String id, String tableName) {

        switch (tableName) {
            case Contexts.TABLE_NAME:
                return getContext(id);
            case Folders.TABLE_NAME:
                return getFolder(id);
            case Tasks.TABLE_NAME:
                return getTask(id);
        }

        return null;
    }

    /**
     * Given the ID of an attachment, get it into the data model
     */
    public Attachment getAttachment(String id) {

        SQLiteDatabase db            = dbHelper.getReadableDatabase();
        String         selection     = Attachments.COLUMN_ID + " = ?";
        String[]       selectionArgs = {id};

        Cursor cursor = dbHelper.query(db, Attachments.TABLE_NAME, Attachments.columns, selection,
                                       selectionArgs);
        cursor.moveToFirst();

        Attachment result = getAttachmentFromCursor(cursor);

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Fetch attachment data from a cursor.
     */
    private Attachment getAttachmentFromCursor(Cursor cursor) {

        Attachment attachment = new Attachment();

        attachment.id = dbHelper.getString(cursor, Attachments.COLUMN_ID.name);
        attachment.name = dbHelper.getString(cursor, Attachments.COLUMN_NAME.name);
        attachment.parentID = dbHelper.getString(cursor, Attachments.COLUMN_PARENT_ID.name);
        attachment.path = dbHelper.getString(cursor, Attachments.COLUMN_PATH.name);
        // attachment.preview = dbHelper.getString(cursor, Attachments.COLUMN_PNG_PREVIEW.name);
        // TODO
        attachment.added = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_ADDED.name);
        attachment.modified = dbHelper.getDate(cursor, Attachments.COLUMN_DATE_MODIFIED.name);

        return attachment;
    }

    /**
     * Just gets the name of a context, given its ID
     */
    public String getContextName(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Contexts.TABLE_NAME, new String[]{Contexts.COLUMN_NAME.name},
                               Contexts.COLUMN_ID + " = ?", new String[]{id});

        cursor.moveToFirst();
        String name = dbHelper.getString(cursor, Contexts.COLUMN_NAME.name);

        cursor.close();
        db.close();

        return name;
    }

    /**
     * Gets all contexts given a selection string and argument. Usually should be accessed by
     * other methods.
     */
    public List<Entry> getContexts(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Contexts.TABLE_NAME, Contexts.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getContextFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    /**
     * Shorthand for empty argument, will get all contexts
     */
    public List<Entry> getContexts() {

        return getContexts(null, null);
    }

    /**
     * Given a context ID, get all child contexts
     */
    public List<Entry> getContexts(Entry parent) {

        return getContexts(ACTIVE + AND + Contexts.COLUMN_PARENT_ID + " = ?",
                           new String[]{parent.id});
    }

    public List<Entry> getContextChildren(Entry parent) {

        List<Entry> result = new ArrayList<>();

        result.addAll(getContexts(parent));
        result.addAll(getTasksWithContext(parent.id));

        return result;
    }

    public List<Entry> getTopLevelContexts() {

        List<Entry> result = new ArrayList<>();

        String selection = TOP_LEVEL + AND + ACTIVE;
        result.add(getNoContext());
        result.addAll(getContexts(selection, null));

        return result;
    }

    public Context getNoContext() {

        SQLiteDatabase db        = dbHelper.getReadableDatabase();
        Context        noContext = new Context();

        noContext.name = mContext.getString(R.string.no_context);
        noContext.countAvailable = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + AVAILABLE);
        noContext.countDueSoon = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + DUE_SOON);
        noContext.countOverdue = (int) DatabaseUtils
                .queryNumEntries(db, Tasks.TABLE_NAME, NO_CONTEXT + AND + OVERDUE);
        noContext.id = "NO_CONTEXT";

        db.close();
        return noContext;
    }

    public Entry getContext(String id) {

        return getContexts(Contexts.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Context getContextFromCursor(Cursor cursor) {

        Context context = new Context();

        // Base methods
        context.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        context.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        context.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        context.active = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE.name);
        context.activeEffective = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE_EFFECTIVE.name);
        context.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        context.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        context.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED.name);
        context.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON.name);
        context.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        context.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        context.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        context.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        context.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        context.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK.name);

        // Context methods
        context.altitude = dbHelper.getLong(cursor, Contexts.COLUMN_ALTITUDE.name);
        context.latitude = dbHelper.getLong(cursor, Contexts.COLUMN_LATITUDE.name);
        context.locationName = dbHelper.getString(cursor, Contexts.COLUMN_LOCATION_NAME.name);
        context.longitude = dbHelper.getLong(cursor, Contexts.COLUMN_LONGITUDE.name);
        context.onHold = dbHelper.getBoolean(cursor, Contexts.COLUMN_ON_HOLD.name);
        context.radius = dbHelper.getLong(cursor, Contexts.COLUMN_RADIUS.name);

        return context;
    }

    public List<Entry> getFolders(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Folders.TABLE_NAME, Folders.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getFolderFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Entry> getFolders(Entry parent) {

        return getFolders(Folders.COLUMN_PARENT_ID + " = ?", new String[]{parent.id});
    }

    public List<Entry> getFolders() {

        return getFolders(null, null);
    }

    public List<Entry> getTopLevelFolders() {

        return getFolders(TOP_LEVEL + ORDER_BY_RANK, null);
    }

    public Entry getFolder(String id) {

        return getFolders(Folders.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Folder getFolderFromCursor(Cursor cursor) {

        Folder folder = new Folder();

        // Base methods
        folder.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        folder.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        folder.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        folder.active = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE.name);
        folder.activeEffective = dbHelper.getBoolean(cursor, Entries.COLUMN_ACTIVE_EFFECTIVE.name);
        folder.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        folder.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        folder.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED.name);
        folder.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON.name);
        folder.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        folder.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        folder.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        folder.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        folder.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        folder.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK.name);

        return folder;
    }

    /**
     * Get project name, given id
     */
    public String getProjectName(String id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.query(db, Tasks.TABLE_NAME, new String[]{Tasks.COLUMN_NAME.name},
                                       Tasks.COLUMN_ID + " = ?", new String[]{id});

        cursor.moveToFirst();
        String name = dbHelper.getString(cursor, Tasks.COLUMN_NAME.name);

        cursor.close();
        db.close();

        return name;
    }

    public List<Entry> getTasks(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                dbHelper.query(db, Tasks.TABLE_NAME, Tasks.columns, selection, selectionArgs);

        List<Entry> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getTaskFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Entry> getTasks(Entry parent) {

        String selection = REMAINING + AND + Entries.COLUMN_PARENT_ID + " = ?" + ORDER_BY_RANK;
        return getTasks(selection, new String[]{parent.id});
    }

    public List<Entry> getTasks() {

        String selection =
                REMAINING + AND + HAS_DUE + " ORDER BY COALESCE(" + Tasks.COLUMN_DATE_DUE + ", " +
                Tasks.COLUMN_DATE_DUE_EFFECTIVE + ")";
        return getTasks(selection, null);
    }

    public List<Entry> getTasksInInbox() {

        return getTasks(IN_INBOX + AND + REMAINING + ORDER_BY_RANK, null);
    }

    public List<Entry> getChildren(Entry parent) {

        List<Entry> entries = new ArrayList<>();

        entries.addAll(getTasks(parent));
        entries.addAll(getFolders(parent));

        Collections.sort(entries);

        return entries;
    }

    public List<Entry> getTopLevelProjects() {

        // (and folders)

        List<Entry> topLevel = new ArrayList<>();

        String selection =
                IS_PROJECT + AND + REMAINING + AND + TOP_LEVEL + AND + Tasks.COLUMN_PROJECT_STATUS +
                " = 'active'" + ORDER_BY_RANK;
        topLevel.addAll(getTasks(selection, null));
        topLevel.addAll(getTopLevelFolders());

        Collections.sort(topLevel);

        return topLevel;
    }

    public List<Entry> getFlagged() {

        String selection =
                "(" + Tasks.COLUMN_FLAGGED + " = 1 OR " + Tasks.COLUMN_FLAGGED_EFFECTIVE + " = 1)" +
                AND + AVAILABLE + AND + Tasks.COLUMN_INBOX + " = 0" + ORDER_BY_RANK;
        return getTasks(selection, null);
    }

    public List<Entry> getTasksWithContext(String contextID) {

        String selection = REMAINING + AND + Tasks.COLUMN_CONTEXT + " = ?" + ORDER_BY_RANK;
        return getTasks(selection, new String[]{contextID});
    }

    public List<Entry> getTasksWithNoContext() {

        String selection = REMAINING + AND + Tasks.COLUMN_CONTEXT + " IS NULL" + ORDER_BY_RANK;
        return getTasks(selection, null);
    }

    public Entry getTask(String id) {

        return getTasks(Tasks.COLUMN_ID + " = ?", new String[]{id}).get(0);
    }

    private Task getTaskFromCursor(Cursor cursor) {

        Task task = new Task();

        // Base methods
        task.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        task.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        task.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        task.countAvailable = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_AVAILABLE.name);
        task.countChildren = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_CHILDREN.name);
        task.countCompleted = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_COMPLETED.name);
        task.countDueSoon = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_DUE_SOON.name);
        task.countOverdue = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_OVERDUE.name);
        task.countRemaining = dbHelper.getInt(cursor, Entries.COLUMN_COUNT_REMAINING.name);
        task.hasChildren = dbHelper.getBoolean(cursor, Entries.COLUMN_HAS_CHILDREN.name);
        task.name = dbHelper.getString(cursor, Entries.COLUMN_NAME.name);
        task.parentID = dbHelper.getString(cursor, Entries.COLUMN_PARENT_ID.name);
        task.rank = dbHelper.getLong(cursor, Entries.COLUMN_RANK.name);

        // Task methods
        task.blocked = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED.name);
        task.blockedByDefer = dbHelper.getBoolean(cursor, Tasks.COLUMN_BLOCKED_BY_DEFER.name);
        task.completeWithChildren =
                dbHelper.getBoolean(cursor, Tasks.COLUMN_COMPLETE_WITH_CHILDREN.name);
        task.context = dbHelper.getString(cursor, Tasks.COLUMN_CONTEXT.name);
        task.dateCompleted = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_COMPLETED.name);
        task.dateDefer = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER.name);
        task.dateDeferEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DEFER_EFFECTIVE.name);
        task.dateDue = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE.name);
        task.dateDueEffective = dbHelper.getDate(cursor, Tasks.COLUMN_DATE_DUE_EFFECTIVE.name);
        task.dueSoon = dbHelper.getBoolean(cursor, Tasks.COLUMN_DUE_SOON.name);
        task.dropped = dbHelper.getBoolean(cursor, Tasks.COLUMN_DROPPED.name);
        task.estimatedTime = dbHelper.getInt(cursor, Tasks.COLUMN_ESTIMATED_TIME.name);
        task.flagged = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED.name);
        task.flaggedEffective = dbHelper.getBoolean(cursor, Tasks.COLUMN_FLAGGED_EFFECTIVE.name);
        task.inInbox = dbHelper.getBoolean(cursor, Tasks.COLUMN_INBOX.name);
        task.next = dbHelper.getString(cursor, Tasks.COLUMN_NEXT.name);
        task.notePlaintext = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_PLAINTEXT.name);
        task.noteXML = dbHelper.getString(cursor, Tasks.COLUMN_NOTE_XML.name);
        task.overdue = dbHelper.getBoolean(cursor, Tasks.COLUMN_OVERDUE.name);
        task.project = dbHelper.getBoolean(cursor, Tasks.COLUMN_PROJECT.name);
        task.projectID = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_ID.name);
        task.lastReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_LAST_REVIEW.name);
        task.nextReview = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_NEXT_REVIEW.name);
        task.reviewInterval = dbHelper.getDate(cursor, Tasks.COLUMN_PROJECT_REPEAT_REVIEW.name);
        task.status = dbHelper.getString(cursor, Tasks.COLUMN_PROJECT_STATUS.name);
        task.repetitionMethod = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_METHOD.name);
        task.repetitionRule = dbHelper.getString(cursor, Tasks.COLUMN_REPETITION_RULE.name);
        task.type = dbHelper.getString(cursor, Tasks.COLUMN_TYPE.name);

        // Project/Context names
        if (task.context != null) {
            task.contextName = getContextName(task.context);
        }
        if (task.projectID != null) {
            task.projectName = getProjectName(task.projectID);
        }

        return task;
    }

    /**
     * Logic for using perspectives to get entries
     */
    public List<Entry> getEntriesFromPerspective(Perspective perspective, Entry parent) {

        List<Entry> items;
        switch (perspective.menuID) {
            case R.id.nav_inbox:
                items = getTasksInInbox();
                break;
            case R.id.nav_projects:
                if (parent == null) {
                    items = getTopLevelProjects();
                } else {
                    items = getChildren(parent);
                }
                break;
            case R.id.nav_contexts:
                if (parent == null) {
                    items = getTopLevelContexts();
                } else if (parent.id.equals("NO_CONTEXT")) {
                    items = getTasksWithNoContext();
                } else {
                    items = getContextChildren(parent);
                }
                break;
            case R.id.nav_flagged:
                items = getFlagged();
                break;
            default:
                items = getTasks();
        }

        return items;
    }

    public List<Perspective> getPerspectives(String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = dbHelper.query(db, Perspectives.TABLE_NAME, Perspectives.columns, selection,
                                       selectionArgs);

        List<Perspective> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            result.add(getPerspectiveFromCursor(cursor));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<Perspective> getPerspectives() {

        List<Perspective> perspectives = getPerspectives(null, null);
        perspectives.add(getForecast());
        return perspectives;
    }

    public Perspective getForecast() {

        Perspective perspective = new Perspective();

        perspective.id = "ProcessForecast";
        perspective.filterDuration = "any";
        perspective.filterFlagged = "any";
        perspective.filterStatus = "due";

        // Don't set `group` or `sort` because Forecast uses a custom grouping/sorting

        perspective.name = "Forecast";
        perspective.menuID = R.id.nav_forecast;
        perspective.themeID = R.style.AppTheme_Red;
        perspective.color = R.color.primary_red;
        perspective.colorStateListID = R.color.state_list_red;
        perspective.icon = R.drawable.ic_forecast_red;

        return perspective;
    }

    public Perspective getBlankPerspective() {

        Perspective perspective = getForecast();
        perspective.color = R.color.primary;
        perspective.colorStateListID = R.color.state_list_purple;
        perspective.themeID = R.style.AppTheme;
        return perspective;
    }

    private Perspective getPerspectiveFromCursor(Cursor cursor) {

        Perspective perspective = new Perspective();

        // Base methods
        perspective.id = dbHelper.getString(cursor, Base.COLUMN_ID.name);
        perspective.added = dbHelper.getDate(cursor, Base.COLUMN_DATE_ADDED.name);
        perspective.modified = dbHelper.getDate(cursor, Base.COLUMN_DATE_MODIFIED.name);

        // Entries methods
        perspective.filterDuration =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_DURATION.name);
        perspective.filterFlagged =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_FLAGGED.name);
        perspective.filterStatus =
                dbHelper.getString(cursor, Perspectives.COLUMN_FILTER_STATUS.name);
        perspective.group = dbHelper.getString(cursor, Perspectives.COLUMN_GROUP.name);
        perspective.name = dbHelper.getString(cursor, Perspectives.COLUMN_NAME.name);
        perspective.sort = dbHelper.getString(cursor, Perspectives.COLUMN_SORT.name);
        perspective.value = dbHelper.getString(cursor, Perspectives.COLUMN_VALUE.name);
        perspective.viewMode = dbHelper.getString(cursor, Perspectives.COLUMN_VIEW_MODE.name);

        String iconName = dbHelper.getString(cursor, Perspectives.COLUMN_ICON.name);

        switch (iconName) {
            case "ProcessFlagged":
                perspective.icon = R.drawable.ic_flag_orange;
                break;
            case "ProcessContexts":
                perspective.icon = R.drawable.ic_contexts_purple;
                break;
            case "ProcessProjects":
                perspective.icon = R.drawable.ic_projects_blue;
                break;
            case "ProcessInbox":
            default:
                perspective.icon = R.drawable.ic_inbox_bluegrey;
                break;
        }

        switch (perspective.id) {
            case "ProcessFlagged":
                perspective.color = R.color.primary_orange;
                perspective.colorStateListID = R.color.state_list_orange;
                perspective.menuID = R.id.nav_flagged;
                perspective.themeID = R.style.AppTheme_Orange;
                break;
            case "ProcessContexts":
                perspective.color = R.color.primary;
                perspective.colorStateListID = R.color.state_list_purple;
                perspective.menuID = R.id.nav_contexts;
                perspective.themeID = R.style.AppTheme;
                break;
            case "ProcessProjects":
                perspective.color = R.color.primary_blue;
                perspective.colorStateListID = R.color.state_list_blue;
                perspective.menuID = R.id.nav_projects;
                perspective.themeID = R.style.AppTheme_Blue;
                break;
            case "ProcessInbox":
                perspective.color = R.color.primary_blue_grey;
                perspective.colorStateListID = R.color.state_list_blue_grey;
                perspective.menuID = R.id.nav_inbox;
                perspective.themeID = R.style.AppTheme_BlueGrey;
                break;
            default:
                perspective.color = R.color.primary_blue_grey;
                perspective.colorStateListID = R.color.state_list_blue_grey;
                perspective.themeID = R.style.AppTheme_BlueGrey;
                // Generate a random number for the other menu IDs, fine as long as it's saved
                perspective.menuID = random.nextInt();
                break;
        }

        return perspective;
    }
}
