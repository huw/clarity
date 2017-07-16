package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.EntryIDConverter
import nu.huw.clarity.model.Attachment
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface AttachmentDao {

    @Query("Select * from Attachment where ParentID = :arg0")
    fun getAttachmentsFromTask(@TypeConverters(EntryIDConverter::class) task: Task): LiveData<List<Attachment>>

    @Query("Select * from Attachment where ID = :arg0")
    fun getAttachmentFromID(id: ID): LiveData<Attachment>

    @Insert
    fun addAttachment(attachment: Attachment)

}