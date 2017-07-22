package nu.huw.clarity.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import nu.huw.clarity.ClarityApplication

@Module
class AppModule(val app: ClarityApplication) {

    @Provides
    fun applicationContext(): Context = app

}