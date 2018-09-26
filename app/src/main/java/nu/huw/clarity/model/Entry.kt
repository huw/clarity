package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime

@Entity
@TypeConverters(Converters::class)
open class Entry(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), var countAvailable: Long = 0, var countChildren: Long = 0, var countCompleted: Long = 0, var countDueSoon: Long = 0, var countOverdue: Long = 0, var countRemaining: Long = 0, var name: String = "", var parentID: ID? = null, var rank: Long = 0, var status: StatusState = StatusState.ACTIVE) : Base(id, dateAdded, dateModified), Comparable<Entry> {
    override fun compareTo(other: Entry): Int = compareValuesBy(this, other, { it.rank })
}
