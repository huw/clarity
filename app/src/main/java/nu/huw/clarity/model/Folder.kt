package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime

@Entity
@TypeConverters(Converters::class)
class Folder(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), countAvailable: Long = 0, countChildren: Long = 0, countCompleted: Long = 0, countDueSoon: Long = 0, countOverdue: Long = 0, countRemaining: Long = 0, name: String = "", parentID: ID? = null, rank: Long = 0, status: StatusState = StatusState.ACTIVE) : Entry(id, dateAdded, dateModified, countAvailable, countChildren, countCompleted, countDueSoon, countOverdue, countRemaining, name, parentID, rank, status)