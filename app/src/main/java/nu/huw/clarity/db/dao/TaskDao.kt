package nu.huw.clarity.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import nu.huw.clarity.db.ContextIDConverter
import nu.huw.clarity.db.IDConverter
import nu.huw.clarity.db.ProjectIDConverter
import nu.huw.clarity.model.*

@Dao interface TaskDao {

    @Query("Select * from Task")
    fun getAll(): List<Task>

    @Query("Select * from Task where inInbox = 1")
    fun getInbox(): List<Task>

    @Query("Select * from Task where ID = :header")
    fun getProjectFromHeader(@TypeConverters(ProjectIDConverter::class) header: Header): Task

    @Query("Select * from Task where isProject = 1 and parentID = :parent")
    fun getProjectsFromNonNullParent(@TypeConverters(IDConverter::class) parent: Folder): List<Task>

    @Query("Select * from Task where isProject = 1 and parentID is null")
    fun getTopLevelProjects(): List<Task>

    @Query("Select * from Task where isProject = 0 and parentID = :parent")
    fun getTasksFromNonNullParent(@TypeConverters(IDConverter::class) parent: Task): List<Task>

    @Query("Select * from Task where isProject = 0 and parentID is null")
    fun getTopLevelTasks(): List<Task>

    @Query("Select * from Task where isProject = 0 and contextID = :context")
    fun getFromContext(@TypeConverters(IDConverter::class) context: Context): List<Task>

    @Query("Select * from Task where isProject = 0 and projectID = :project")
    fun getFromProject(@TypeConverters(IDConverter::class) project: Task): List<Task>

    @Query("Select * from Task where id = :task")
    fun getProjectFromTask(@TypeConverters(ProjectIDConverter::class) task: Task): Task

    @Query("Select * from Task where ID = :id")
    fun getFromID(id: ID): Task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(task: Task)

    @Delete
    fun delete(task: Task)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(task: Task)

}