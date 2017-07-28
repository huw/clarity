package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.ContextIDConverter
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface ContextDao {

    @Query("Select * from Context where ID = :task")
    fun getFromTask(@TypeConverters(ContextIDConverter::class) task: Task): LiveData<Context>

    @Query("Select * from Context where parentID = :parent")
    fun getFromNonNullParent(@TypeConverters(IDConverter::class) parent: Context): LiveData<List<Context>>

    @Query("Select * from Context where parentID is null")
    fun getTopLevel(): LiveData<List<Context>>

    @Query("Select * from Context where ID = :id")
    fun getFromID(id: ID): LiveData<Context>

    @Query("Select * from Context")
    fun getAll(): LiveData<List<Context>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(context: Context)

    @Delete
    fun delete(context: Context)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(context: Context)

}

/**
 * Controls flow between two queries depending on nullness
 */
fun ContextDao.getFromParent(parent: Context?): LiveData<List<Context>> = if (parent != null) getFromNonNullParent(parent) else getTopLevel()