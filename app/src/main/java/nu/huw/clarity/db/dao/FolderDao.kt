package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.model.Folder
import nu.huw.clarity.model.ID

@Dao interface FolderDao {

    @Query("Select * from Folder")
    fun getAll(): LiveData<List<Folder>>

    @Query("Select * from Folder where parentID = :parent")
    fun getFromNonNullParent(@TypeConverters(IDConverter::class) parent: Folder): LiveData<List<Folder>>

    @Query("Select * from Folder where parentID is null")
    fun getTopLevel(): LiveData<List<Folder>>

    @Query("Select * from Folder where ID = :id")
    fun getFromID(id: ID): LiveData<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(folder: Folder)

    @Delete
    fun delete(folder: Folder)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(folder: Folder)

}

/**
 * Controls flow between two queries depending on nullness
 */
fun FolderDao.getFromParent(parent: Folder?): LiveData<List<Folder>> = if (parent != null) getFromNonNullParent(parent) else getTopLevel()