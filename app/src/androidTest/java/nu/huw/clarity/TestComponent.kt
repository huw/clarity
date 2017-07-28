package nu.huw.clarity

import dagger.Component
import nu.huw.clarity.dagger.AppModule
import nu.huw.clarity.dagger.DatabaseModule
import nu.huw.clarity.db.AttachmentDaoTest
import nu.huw.clarity.db.ContextDaoTest
import nu.huw.clarity.db.FolderDaoTest
import nu.huw.clarity.db.PerspectiveDaoTest
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, DatabaseModule::class))
interface TestComponent {
    fun inject(test: AttachmentDaoTest)
    fun inject(test: ContextDaoTest)
    fun inject(test: FolderDaoTest)
    fun inject(test: PerspectiveDaoTest)
}