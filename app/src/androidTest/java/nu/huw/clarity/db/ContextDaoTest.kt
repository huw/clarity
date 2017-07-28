package nu.huw.clarity.db

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.DaggerTestComponent
import nu.huw.clarity.TestComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.db.dao.getFromParent
import nu.huw.clarity.model.Context
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class ContextDaoTest {

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
        val context = Context(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ"))
        db.contextDao().add(context)
    }

    @After
    fun teardown() {
        // Remove data
        db.contextDao().delete(Context(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ")))
    }

    @Test
    fun getAll() {
        val list = db.contextDao().getAll()
        assertFalse(list.blockingObserve()?.isEmpty()!!)
    }

    @Test
    fun getFromId() {
        val context = db.contextDao().getFromID(ID("eVJuS9Id_wJ")).blockingObserve()
        assertEquals(ID("eVJuS9Id_wJ"), context?.id)
    }

    @Test
    fun getFromParent() {
        val contexts = db.contextDao().getFromParent(Context(ID("eVJuS9Id_wZ"))).blockingObserve()
        assertTrue(contexts!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getFromNullParent() {
        val contexts = db.contextDao().getFromParent(null).blockingObserve()
        assertFalse(contexts!!.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getFromTask() {
        val context = db.contextDao().getFromTask(Task(contextID = ID("eVJuS9Id_wJ"))).blockingObserve()
        assertEquals(ID("eVJuS9Id_wJ"), context?.id)
    }
}