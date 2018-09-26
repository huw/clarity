package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.ContextIDConverter
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.Header
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface ContextDao {

    @Query("Select * from Context where ID = :task")
    fun getFromTask(@TypeConverters(ContextIDConverter::class) task: Task): Context

    @Query("Select * from Context where ID = :header")
    fun getFromHeader(@TypeConverters(ContextIDConverter::class) header: Header): Context

    @Query("Select * from Context where parentID = :parent")
    fun getFromNonNullParent(@TypeConverters(IDConverter::class) parent: Context): List<Context>

    @Query("Select * from Context where parentID is null")
    fun getTopLevel(): List<Context>

    @Query("Select * from Context where ID = :id")
    fun getFromID(id: ID): Context

    @Query("Select * from Context")
    fun getAll(): List<Context>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(context: Context)

    @Delete
    fun delete(context: Context)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(context: Context)

}