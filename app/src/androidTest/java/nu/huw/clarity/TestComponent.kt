package nu.huw.clarity

import dagger.Component
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.dagger.DatabaseModule
import nu.huw.clarity.db.*
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, DatabaseModule::class))
interface TestComponent {
    fun inject(test: AttachmentDaoTest)
    fun inject(test: ContextDaoTest)
    fun inject(test: FolderDaoTest)
    fun inject(test: PerspectiveDaoTest)
    fun inject(test: TaskDaoTest)
}