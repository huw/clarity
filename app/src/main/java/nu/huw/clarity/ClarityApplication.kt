package nu.huw.clarity

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import nu.huw.clarity.dagger.AppComponent
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.dagger.DaggerAppComponent

class ClarityApplication : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        component.inject(this)
    }
}
