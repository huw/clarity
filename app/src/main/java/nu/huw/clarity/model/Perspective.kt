package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime

@Entity
@TypeConverters(Converters::class)
class Perspective(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), var filterDuration: DurationFilterState = DurationFilterState.NONE, var filterFlagged: FlaggedFilterState = FlaggedFilterState.NONE, var filterStatus: StatusFilterState = StatusFilterState.NONE, var collation: CollationState = CollationState.NONE, var name: String = "", var sort: SortState = SortState.NONE, var data: String? = null, var viewMode: ViewModeState = ViewModeState.CONTEXT) : Base(id, dateAdded, dateModified)