package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.TypeConverters
import nu.huw.clarity.db.EntryIDConverter
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.ID

@Dao interface ContextDao {

    @Query("Select * from Context where parentID = :arg0")
    fun getContextsFromParent(@TypeConverters(EntryIDConverter::class) parent: Context): LiveData<List<Context>>

    @Query("Select * from Context where ID = :arg0")
    fun getContextFromID(id: ID): LiveData<Context>

    @Query("Select * from Context")
    fun getAllContexts(): LiveData<List<Context>>

    @Insert
    fun addContext(context: Context)

}