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
    fun dateToString(date: LocalDateTime?) = date?.atZone(ZoneId.systemDefault())?.toInstant().toString()

    @TypeConverter
    fun stringToDate(string: String?): LocalDateTime = LocalDateTime.ofInstant(Instant.parse(string), ZoneId.systemDefault())

    @TypeConverter
    fun durationToString(duration: Duration?) = duration.toString()

    @TypeConverter
    fun stringToDuration(string: String?): Duration = Duration.parse(string)

    @TypeConverter
    fun IDToString(id: ID?) = id?.value

    @TypeConverter
    fun stringToID(string: String?) = ID(string!!)

    @TypeConverter
    fun fileToString(file: File?) = file?.path

    @TypeConverter
    fun stringToFile(string: String?) = File(string)

    @TypeConverter fun droppedStateToString(state: DroppedState?) = state?.name
    @TypeConverter fun flaggedStateToString(state: FlaggedState?) = state?.name
    @TypeConverter fun blockedStateToString(state: BlockedState?) = state?.name
    @TypeConverter fun statusStateToString(state: StatusState?) = state?.name
    @TypeConverter fun typeStateToString(state: TypeState?) = state?.name
    @TypeConverter fun statusFilterStateToString(state: StatusFilterState?) = state?.name
    @TypeConverter fun flaggedFilterStateToString(state: FlaggedFilterState?) = state?.name
    @TypeConverter fun durationFilterStateToString(state: DurationFilterState?) = state?.name
    @TypeConverter fun collationStateToString(state: CollationState?) = state?.name
    @TypeConverter fun viewModeStateToString(state: ViewModeState?) = state?.name
    @TypeConverter fun sortStateToString(state: SortState?) = state?.name

    @TypeConverter fun stringToDroppedState(string: String?) = DroppedState.valueOf(string!!)
    @TypeConverter fun stringToFlaggedState(string: String?) = FlaggedState.valueOf(string!!)
    @TypeConverter fun stringToBlockedState(string: String?) = BlockedState.valueOf(string!!)
    @TypeConverter fun stringToStatusState(string: String?) = StatusState.valueOf(string!!)
    @TypeConverter fun stringToTypeState(string: String?) = TypeState.valueOf(string!!)
    @TypeConverter fun stringToStatusFilterState(string: String?) = StatusFilterState.valueOf(string!!)
    @TypeConverter fun stringToFlaggedFilterState(string: String?) = FlaggedFilterState.valueOf(string!!)
    @TypeConverter fun stringToDurationFilterState(string: String?) = DurationFilterState.valueOf(string!!)
    @TypeConverter fun stringToCollationState(string: String?) = CollationState.valueOf(string!!)
    @TypeConverter fun stringToViewModeState(string: String?) = ViewModeState.valueOf(string!!)
    @TypeConverter fun stringToSortState(string: String?) = SortState.valueOf(string!!)
}

class EntryIDConverter {

    @TypeConverter fun taskToID(task: Task?) = task?.id?.value
    @TypeConverter fun contextToID(context: Context?) = context?.id?.value

}