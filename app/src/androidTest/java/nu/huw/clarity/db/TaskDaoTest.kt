package nu.huw.clarity.db

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.DaggerTestComponent
import nu.huw.clarity.TestComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.db.dao.getProjectsFromParent
import nu.huw.clarity.db.dao.getTasksFromParent
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.Folder
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class TaskDaoTest {

    @Inject
    lateinit var db: AppDatabase

    val component: TestComponent by lazy {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as ClarityApplication
        DaggerTestComponent.builder().appModule(AppModule(app)).build()
    }

    @Before
    fun setup() {
        component.inject(this)

        // Setup data
        val task = Task(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ"), contextID = ID("eVJuS9Id_wY"), projectID = ID("eVJuS9Id_wX"), inInbox = true)
        val project = Task(ID("eVJuS9Id_wX"), name = "random", parentID = ID("eVJuS9Id_wP"), inInbox = true, isProject = true)
        db.taskDao().add(task)
        db.taskDao().add(project)
    }

    @After
    fun teardown() {
        // Remove data
        db.taskDao().delete(Task(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ"), contextID = ID("eVJuS9Id_wY"), projectID = ID("eVJuS9Id_wX"), inInbox = true))
        db.taskDao().delete(Task(ID("eVJuS9Id_wX"), name = "random", parentID = ID("eVJuS9Id_wP"), inInbox = true, isProject = true))
    }

    @Test
    fun getAll() {
        val list = db.taskDao().getAll()
        Assert.assertFalse(list.blockingObserve()?.isEmpty()!!)
    }

    @Test
    fun getFromId() {
        val task = db.taskDao().getFromID(ID("eVJuS9Id_wJ")).blockingObserve()
        Assert.assertEquals(ID("eVJuS9Id_wJ"), task?.id)
    }

    @Test
    fun getProjectsFromParent() {
        val projects = db.taskDao().getProjectsFromParent(Folder(ID("eVJuS9Id_wP"))).blockingObserve()
        Assert.assertTrue(projects!!.any { it.id == ID("eVJuS9Id_wX") })
    }

    @Test
    fun getProjectsFromNullParent() {
        val projects = db.taskDao().getProjectsFromParent(null).blockingObserve()
        Assert.assertFalse(projects!!.any { it.id == ID("eVJuS9Id_wX") })
    }

    @Test
    fun getTasksFromParent() {
        val tasks = db.taskDao().getTasksFromParent(Task(ID("eVJuS9Id_wZ"))).blockingObserve()
        Assert.assertTrue(tasks!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getTasksFromNullParent() {
        val tasks = db.taskDao().getTasksFromParent(null).blockingObserve()
        Assert.assertFalse(tasks!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getTasksFromInbox() {
        val tasks = db.taskDao().getTasksInInbox().blockingObserve()
        Assert.assertTrue(tasks!!.size >= 2)
        Assert.assertTrue(tasks.all { it.inInbox })
    }

    @Test
    fun getTasksFromContext() {
        val tasks = db.taskDao().getTasksFromContext(Context(ID("eVJuS9Id_wY"))).blockingObserve()
        Assert.assertTrue(tasks!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getTasksFromProject() {
        val tasks = db.taskDao().getTasksFromProject(Task(ID("eVJuS9Id_wX"))).blockingObserve()
        Assert.assertTrue(tasks!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getProjectFromTask() {
        val project = db.taskDao().getProjectFromTask(Task(ID("eVJuS9Id_wJ"), projectID = ID("eVJuS9Id_wX"))).blockingObserve()
        Assert.assertEquals(ID("eVJuS9Id_wX"), project!!.id)
    }
}