package com.secretcom.app.data.local.dao

import androidx.room.*
import com.secretcom.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE meetingId = :meetingId")
    suspend fun getUserByMeetingId(meetingId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY lastLogin DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
