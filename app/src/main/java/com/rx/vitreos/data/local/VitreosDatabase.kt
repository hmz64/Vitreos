package com.rx.vitreos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MessageEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VitreosDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
}