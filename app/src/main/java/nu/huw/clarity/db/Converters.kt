package nu.huw.clarity.db

import android.arch.persistence.room.TypeConverter
import nu.huw.clarity.model.*
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.io.File

class Converters {

    @TypeConverter
    fun dateToString(date: LocalDateTime?) = if (date != null) date.atZone(ZoneId.systemDefault())?.toInstant().toString() else null

    @TypeConverter
    fun stringToDate(string: String?) = if (string != null) LocalDateTime.ofInstant(Instant.parse(string), ZoneId.systemDefault()) else null

    @TypeConverter
    fun durationToString(duration: Duration?) = duration?.toString()

    @TypeConverter
    fun stringToDuration(string: String?) = if (string != null) Duration.parse(string) else null

    @TypeConverter
    fun IDToString(id: ID?) = id?.value

    @TypeConverter
    fun stringToID(string: String?) = if (string != null) ID(string) else null

    @TypeConverter
    fun fileToString(file: File?) = file?.path

    @TypeConverter
    fun stringToFile(string: String?) = if (string != null) File(string) else null

    @TypeConverter fun droppedStateToString(state: DroppedState?) = state?.name
    @TypeConverter fun flaggedStateToString(state: FlaggedState?) = state?.name
    @TypeConverter fun blockedStateToString(state: BlockedState?) = state?.name
    @TypeConverter fun statusStateToString(state: StatusState?) = state?.name
    @TypeConverter fun typeStateToString(state: CompletionRuleState?) = state?.name
    @TypeConverter fun statusFilterStateToString(state: StatusFilterState?) = state?.name
    @TypeConverter fun flaggedFilterStateToString(state: FlaggedFilterState?) = state?.name
    @TypeConverter fun durationFilterStateToString(state: DurationFilterState?) = state?.name
    @TypeConverter fun collationStateToString(state: CollationState?) = state?.name
    @TypeConverter fun viewModeStateToString(state: ViewModeState?) = state?.name
    @TypeConverter fun sortStateToString(state: SortState?) = state?.name
    @TypeConverter fun iconStateToString(state: PerspectiveIconState?) = state?.name
    @TypeConverter fun colorStateToString(state: PerspectiveColorState?) = state?.name

    @TypeConverter fun stringToDroppedState(string: String?) = DroppedState.valueOf(string!!)
    @TypeConverter fun stringToFlaggedState(string: String?) = FlaggedState.valueOf(string!!)
    @TypeConverter fun stringToBlockedState(string: String?) = BlockedState.valueOf(string!!)
    @TypeConverter fun stringToStatusState(string: String?) = StatusState.valueOf(string!!)
    @TypeConverter fun stringToTypeState(string: String?) = CompletionRuleState.valueOf(string!!)
    @TypeConverter fun stringToStatusFilterState(string: String?) = StatusFilterState.valueOf(string!!)
    @TypeConverter fun stringToFlaggedFilterState(string: String?) = FlaggedFilterState.valueOf(string!!)
    @TypeConverter fun stringToDurationFilterState(string: String?) = DurationFilterState.valueOf(string!!)
    @TypeConverter fun stringToCollationState(string: String?) = CollationState.valueOf(string!!)
    @TypeConverter fun stringToViewModeState(string: String?) = ViewModeState.valueOf(string!!)
    @TypeConverter fun stringToSortState(string: String?) = SortState.valueOf(string!!)
    @TypeConverter fun stringToIconState(string: String?) = PerspectiveIconState.valueOf(string!!)
    @TypeConverter fun stringToColorState(string: String?) = PerspectiveColorState.valueOf(string!!)
}

class LooseIDConverter {
    @TypeConverter
    fun stringToID(string: String?) = if (string != null) ID(string, allowAny = true) else null
}

class IDConverter {

    @TypeConverter fun attachmentToID(attachment: Attachment?) = attachment?.id?.value
    @TypeConverter fun contextToID(context: Context?) = context?.id?.value
    @TypeConverter fun folderToID(folder: Folder?) = folder?.id?.value
    @TypeConverter fun perspectiveToID(perspective: Perspective?) = perspective?.id?.value
    @TypeConverter fun taskToID(task: Task?) = task?.id?.value

}

class ParentIDConverter {

    @TypeConverter fun folderToID(folder: Folder?) = folder?.parentID?.value
    @TypeConverter fun contextToID(context: Context?) = context?.parentID?.value
    @TypeConverter fun taskToID(task: Task?) = task?.projectID?.value

}

class ContextIDConverter {
    @TypeConverter fun taskToID(task: Task?) = task?.contextID?.value
    @TypeConverter fun headerToID(header: Header?) = header?.contextID?.value
}

class ProjectIDConverter {
    @TypeConverter fun taskToID(task: Task?) = task?.projectID?.value
    @TypeConverter fun headerToID(header: Header?) = header?.projectID?.value
}

class FolderIDConverter {
    @TypeConverter fun headerToID(header: Header?) = header?.folderID?.value
}

class NextIDConverter {
    @TypeConverter fun taskToID(task: Task?) = task?.nextID?.value
}