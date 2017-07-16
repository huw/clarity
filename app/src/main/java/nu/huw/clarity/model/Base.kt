package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime


@Entity
@TypeConverters(Converters::class)
open class Base(@PrimaryKey var id: ID = ID(), var dateAdded: LocalDateTime = LocalDateTime.now(), var dateModified: LocalDateTime = LocalDateTime.now()) {
    /**
     * Items that inherit from base compare with other items by their IDs
     */
    override fun equals(other: Any?): Boolean {
        return other is Base && id == other.id
    }
}