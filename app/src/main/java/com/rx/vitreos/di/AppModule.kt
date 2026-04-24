package com.rx.vitreos.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.rx.vitreos.data.local.MessageDao
import com.rx.vitreos.data.local.UserDao
import com.rx.vitreos.data.local.VitreosDatabase
import com.rx.vitreos.data.remote.SocketManager
import com.rx.vitreos.data.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vitreos_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideVitreosDatabase(
        @ApplicationContext context: Context
    ): VitreosDatabase {
        return Room.databaseBuilder(
            context,
            VitreosDatabase::class.java,
            "vitreos_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: VitreosDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: VitreosDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSocketManager(): SocketManager {
        return SocketManager()
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        messageDao: MessageDao,
        userDao: UserDao,
        socketManager: SocketManager
    ): ChatRepository {
        return ChatRepository(messageDao, userDao, socketManager)
    }

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}