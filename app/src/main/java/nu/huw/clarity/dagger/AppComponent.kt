package nu.huw.clarity.dagger

import dagger.Component
import nu.huw.clarity.ClarityApplication
import nu.huw.clarity.db.DataModelHelper
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, DatabaseModule::class))
interface AppComponent {
    fun inject(app: ClarityApplication)
    fun inject(dataModelHelper: DataModelHelper)
}