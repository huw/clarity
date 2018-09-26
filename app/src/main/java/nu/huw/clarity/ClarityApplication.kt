package nu.huw.clarity

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import nu.huw.clarity.dagger.AppComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.dagger.DaggerAppComponent
import nu.huw.clarity.db.AppDatabase
import nu.huw.clarity.model.Task
import javax.inject.Inject

class ClarityApplication : Application() {

    @Inject lateinit var db: AppDatabase

    val component: AppComponent by lazy {
        DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        AndroidThreeTen.init(this)
    }
}
