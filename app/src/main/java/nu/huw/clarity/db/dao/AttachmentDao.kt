package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.model.Attachment
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface AttachmentDao {

    @Query("Select * from Attachment where ParentID = :task")
    fun getFromTask(@TypeConverters(IDConverter::class) task: Task): List<Attachment>

    @Query("Select * from Attachment where ID = :id")
    fun getFromID(id: ID): Attachment

    @Query("Select * from Attachment")
    fun getAll(): List<Attachment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(attachment: Attachment)

    @Delete
    fun delete(attachment: Attachment)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(attachment: Attachment)

}