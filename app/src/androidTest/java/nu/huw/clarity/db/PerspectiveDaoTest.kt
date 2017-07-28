package nu.huw.clarity.db

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.DaggerTestComponent
import nu.huw.clarity.R
import nu.huw.clarity.TestComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.db.dao.getForecast
import nu.huw.clarity.model.ID
import nu.huw.clarity.model.Perspective
import nu.huw.clarity.model.PerspectiveColorState
import nu.huw.clarity.model.PerspectiveIconState
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@SmallTest
class PerspectiveDaoTest {

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
        val perspective = Perspective(ID("eVJuS9Id_wJ"), name = "random", iconState = PerspectiveIconState.NEARBY, colorState = PerspectiveColorState.GREEN)
        db.perspectiveDao().add(perspective)
    }

    @After
    fun teardown() {
        // Remove data
        db.perspectiveDao().delete(Perspective(ID("eVJuS9Id_wJ"), name = "random", iconState = PerspectiveIconState.NEARBY, colorState = PerspectiveColorState.GREEN))
    }

    @Test
    fun getAll() {
        val list = db.perspectiveDao().getAll()
        Assert.assertFalse(list.blockingObserve()?.isEmpty()!!)
    }

    @Test
    fun getFromId() {
        val perspective = db.perspectiveDao().getFromID(ID("eVJuS9Id_wJ")).blockingObserve()
        Assert.assertEquals(ID("eVJuS9Id_wJ"), perspective?.id)
        Assert.assertEquals(R.color.primary_green, perspective?.colorID) // Tests whether helper methods work
    }

    @Test
    fun getForecast() {
        val forecast = db.perspectiveDao().getForecast()
        Assert.assertEquals(ID("ProcessForecast", allowAny = true), forecast.id)
        Assert.assertEquals(PerspectiveColorState.RED, forecast.colorState)
    }

}
