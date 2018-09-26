package nu.huw.clarity.db

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.DaggerTestComponent
import nu.huw.clarity.TestComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.model.Folder
import nu.huw.clarity.model.Header
import nu.huw.clarity.model.ID
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class FolderDaoTest {

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
        val folder = Folder(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ"))
        db.folderDao().add(folder)
    }

    @After
    fun teardown() {
        // Remove data
        db.folderDao().delete(Folder(ID("eVJuS9Id_wJ"), name = "random", parentID = ID("eVJuS9Id_wZ")))
    }

    @Test
    fun getAll() {
        val list = db.folderDao().getAll()
        Assert.assertFalse(list.isEmpty())
    }

    @Test
    fun getFromId() {
        val folder = db.folderDao().getFromID(ID("eVJuS9Id_wJ"))
        Assert.assertEquals(ID("eVJuS9Id_wJ"), folder.id)
    }

    @Test
    fun getFromParent() {
        val folders = db.folderDao().getFromParent(Folder(ID("eVJuS9Id_wZ")))
        Assert.assertTrue(folders.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getFromNullParent() {
        val folders = db.folderDao().getFromParent(null)
        Assert.assertFalse(folders.any { it.id == ID("eVJuS9Id_wJ") })
    }

    @Test
    fun getFromHeader() {
        val folder = db.folderDao().getFromHeader(Header("Test", folderID = ID("eVJuS9Id_wJ")))
        Assert.assertEquals(ID("eVJuS9Id_wJ"), folder.id)
    }

}