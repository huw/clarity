package nu.huw.clarity.db

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.DaggerTestComponent
import nu.huw.clarity.TestComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.model.Attachment
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Task
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDateTime
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class AttachmentTest {

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
        val sent = Attachment(ID("eVJuS9Id_wJ"), LocalDateTime.now(), LocalDateTime.now(), "random", ID("eVJuS9Id_wJ"), File(""))
        db.attachmentDao().addAttachment(sent)
    }

    @After
    fun teardown() {
        // Remove data
        db.attachmentDao().delete(Attachment(ID("eVJuS9Id_wJ"), LocalDateTime.now(), LocalDateTime.now(), "random", ID("eVJuS9Id_wJ"), File("")))
    }

    @Test
    fun getAllAttachments() {
        val listOfAttachments = db.attachmentDao().getAllAttachments()
        val attachment = listOfAttachments.blockingObserve()?.first()
        assertEquals(ID("eVJuS9Id_wJ"), attachment?.id)
    }

    @Test
    fun getAttachmentFromId() {
        val attachment = db.attachmentDao().getAttachmentFromID(ID("eVJuS9Id_wJ")).blockingObserve()
        assertEquals(ID("eVJuS9Id_wJ"), attachment?.id)
    }

    @Test
    fun getAttachmentFromTask() {
        val attachment = db.attachmentDao().getAttachmentsFromTask(Task(ID("eVJuS9Id_wJ"))).blockingObserve()
        assertTrue(attachment!!.any { it.id == ID("eVJuS9Id_wJ") })
    }


}