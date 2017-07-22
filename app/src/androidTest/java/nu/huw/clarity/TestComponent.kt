package nu.huw.clarity

import dagger.Component
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.dagger.DatabaseModule
import nu.huw.clarity.db.AttachmentTest
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, DatabaseModule::class))
interface TestComponent {
    fun inject(test: AttachmentTest)
}