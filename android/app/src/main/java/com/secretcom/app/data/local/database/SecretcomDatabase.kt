package com.secretcom.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.secretcom.app.data.local.dao.AppSettingDao
import com.secretcom.app.data.local.dao.RecordingDao
import com.secretcom.app.data.local.dao.UserDao
import com.secretcom.app.data.local.entity.AppSettingEntity
import com.secretcom.app.data.local.entity.RecordingEntity
import com.secretcom.app.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, RecordingEntity::class, AppSettingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SecretcomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recordingDao(): RecordingDao
    abstract fun appSettingDao(): AppSettingDao
}
