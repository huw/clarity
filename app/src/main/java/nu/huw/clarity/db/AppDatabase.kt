package nu.huw.clarity.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.dao.AttachmentDao
import nu.huw.clarity.db.dao.ContextDao
import nu.huw.clarity.db.dao.FolderDao
import nu.huw.clarity.db.dao.PerspectiveDao
import nu.huw.clarity.model.*

@Database(entities = arrayOf(Attachment::class, Context::class, Folder::class, Perspective::class, Task::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attachmentDao(): AttachmentDao
    abstract fun contextDao(): ContextDao
    abstract fun folderDao(): FolderDao
    abstract fun perspectiveDao(): PerspectiveDao
}