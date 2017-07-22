package nu.huw.clarity.dagger

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import nu.huw.clarity.db.AppDatabase
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun appDatabase(context: Context): AppDatabase = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "clarity.db").build()

}