package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.ContextIDConverter
import nu.huw.clarity.db.FolderIDConverter
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.Folder
import nu.huw.clarity.model.Header
import nu.huw.clarity.model.ID

@Dao interface FolderDao {

    @Query("Select * from Folder")
    fun getAll(): List<Folder>

    @Query("Select * from Folder where ID = :header")
    fun getFromHeader(@TypeConverters(FolderIDConverter::class) header: Header): Folder

    @Query("Select * from Folder where parentID = :parent")
    fun getFromNonNullParent(@TypeConverters(IDConverter::class) parent: Folder): List<Folder>

    @Query("Select * from Folder where parentID is null")
    fun getTopLevel(): List<Folder>

    @Query("Select * from Folder where ID = :id")
    fun getFromID(id: ID): Folder

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(folder: Folder)

    @Delete
    fun delete(folder: Folder)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(folder: Folder)

}