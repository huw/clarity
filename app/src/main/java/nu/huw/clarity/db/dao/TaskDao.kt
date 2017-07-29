package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.db.ProjectIDConverter
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.Folder
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task

@Dao interface TaskDao {

    @Query("Select * from Task")
    fun getAll(): LiveData<List<Task>>

    @Query("Select * from Task where inInbox = 1")
    fun getTasksInInbox(): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 1 and parentID = :parent")
    fun getProjectsFromNonNullParent(@TypeConverters(IDConverter::class) parent: Folder): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 1 and parentID is null")
    fun getTopLevelProjects(): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 0 and parentID = :parent")
    fun getTasksFromNonNullParent(@TypeConverters(IDConverter::class) parent: Task): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 0 and parentID is null")
    fun getTopLevelTasks(): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 0 and contextID = :context")
    fun getTasksFromContext(@TypeConverters(IDConverter::class) context: Context): LiveData<List<Task>>

    @Query("Select * from Task where isProject = 0 and projectID = :project")
    fun getTasksFromProject(@TypeConverters(IDConverter::class) project: Task): LiveData<List<Task>>

    @Query("Select * from Task where id = :task")
    fun getProjectFromTask(@TypeConverters(ProjectIDConverter::class) task: Task): LiveData<Task>

    @Query("Select * from Task where ID = :id")
    fun getFromID(id: ID): LiveData<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(task: Task)

    @Delete
    fun delete(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(task: Task)

}

/**
 * Convenience methods for controlling flow
 */
fun TaskDao.getProjectsFromParent(parent: Folder?): LiveData<List<Task>> = if (parent != null) getProjectsFromNonNullParent(parent) else getTopLevelProjects()

fun TaskDao.getTasksFromParent(parent: Task?): LiveData<List<Task>> = if (parent != null) getTasksFromNonNullParent(parent) else getTopLevelTasks()