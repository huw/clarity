package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.EntryIDConverter
import nu.huw.clarity.model.Attachment
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface AttachmentDao {

    @Query("Select * from Attachment where ParentID = :task")
    fun getAttachmentsFromTask(@TypeConverters(EntryIDConverter::class) task: Task): LiveData<List<Attachment>>

    @Query("Select * from Attachment where ID = :id")
    fun getAttachmentFromID(id: ID): LiveData<Attachment>

    @Query("Select * from Attachment")
    fun getAllAttachments(): LiveData<List<Attachment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAttachment(attachment: Attachment)

    @Delete
    fun delete(attachment: Attachment)

}