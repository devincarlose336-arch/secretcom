package com.secretcom.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val name: String,
    val role: String,
    val meetingId: String? = null,
    val isActive: Boolean = true,
    val lastLogin: Long = System.currentTimeMillis(),
)

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: Int,
    val roomName: String,
    val filePath: String,
    val duration: Long = 0,
    val fileSize: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val meetingId: String? = null,
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)
