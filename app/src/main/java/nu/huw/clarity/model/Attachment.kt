package nu.huw.clarity.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.Converters
import org.threeten.bp.LocalDateTime
import java.io.File

@Entity
@TypeConverters(Converters::class)
class Attachment(id: ID = ID(), dateAdded: LocalDateTime = LocalDateTime.now(), dateModified: LocalDateTime = LocalDateTime.now(), var name: String = "", var parentID: ID = ID(), var file: File = File("")) : Base(id, dateAdded, dateModified)