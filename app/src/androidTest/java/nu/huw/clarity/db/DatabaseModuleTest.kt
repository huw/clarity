package nu.huw.clarity.db

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModuleTest {

    @Provides
    @Singleton
    fun appDatabase(context: Context): AppDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "clarity-test.db").build()

}