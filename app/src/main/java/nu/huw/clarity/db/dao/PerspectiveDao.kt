package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.LooseIDConverter
import nu.huw.clarity.model.*

@TypeConverters(LooseIDConverter::class)
@Dao interface PerspectiveDao {

    @Query("Select * from Perspective")
    fun getAll(): LiveData<List<Perspective>>

    @Query("Select * from Perspective where ID = :id")
    fun getFromID(id: ID): LiveData<Perspective>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(perspective: Perspective)

    @Delete
    fun delete(perspective: Perspective)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(perspective: Perspective)

}

fun PerspectiveDao.getForecast(): Perspective = Perspective(
        id = ID("ProcessForecast", allowAny = true),
        filterDuration = DurationFilterState.NONE,
        filterFlagged = FlaggedFilterState.NONE,
        filterStatus = StatusFilterState.REMAINING,
        sort = SortState.DUE,
        collation = CollationState.DUE,
        name = "Forecast",
        iconState = PerspectiveIconState.FORECAST,
        colorState = PerspectiveColorState.RED
)