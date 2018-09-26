package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.LooseIDConverter
import nu.huw.clarity.model.*

@TypeConverters(LooseIDConverter::class)
@Dao interface PerspectiveDao {

    @Query("Select * from Perspective")
    fun getAll(): List<Perspective>

    @Query("Select * from Perspective where ID = :id")
    fun getFromID(id: ID): Perspective

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(perspective: Perspective)

    @Delete
    fun delete(perspective: Perspective)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(perspective: Perspective)

}