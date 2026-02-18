package com.secretcom.app.data.repository

import com.secretcom.app.data.local.dao.UserDao
import com.secretcom.app.data.local.entity.UserEntity
import com.secretcom.app.data.preferences.TokenManager
import com.secretcom.app.data.remote.api.*
import com.secretcom.app.domain.model.AuthResponse
import com.secretcom.app.domain.model.User
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) {
    suspend fun login(username: String, password: String): Resource<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.user.toDomain()
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                tokenManager.saveUserInfo(user.id, user.role.value, user.name, user.meetingId)
                userDao.insertUser(user.toEntity())
                Resource.Success(AuthResponse(user, body.accessToken, body.refreshToken))
            } else {
                Resource.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun register(name: String, meetingId: String): Resource<AuthResponse> {
        return try {
            val response = apiService.register(RegisterRequest(name, meetingId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val user = body.user.toDomain()
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                tokenManager.saveUserInfo(user.id, user.role.value, user.name, user.meetingId)
                userDao.insertUser(user.toEntity())
                Resource.Success(AuthResponse(user, body.accessToken, body.refreshToken))
            } else {
                Resource.Error("Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun createAdmin(username: String, password: String, name: String): Resource<String> {
        return try {
            val response = apiService.createAdmin(CreateAdminRequest(username, password, name))
            if (response.isSuccessful) {
                Resource.Success("Admin created successfully")
            } else {
                Resource.Error("Failed to create admin")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create admin")
        }
    }

    suspend fun logout(): Resource<String> {
        return try {
            apiService.logout()
            tokenManager.clearTokens()
            Resource.Success("Logged out")
        } catch (e: Exception) {
            tokenManager.clearTokens()
            Resource.Success("Logged out")
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return tokenManager.accessToken.firstOrNull() != null
    }

    suspend fun getCurrentRole(): UserRole? {
        val role = tokenManager.userRole.firstOrNull() ?: return null
        return UserRole.fromString(role)
    }

    suspend fun getCurrentUserName(): String? {
        return tokenManager.userName.firstOrNull()
    }

    suspend fun getCurrentMeetingId(): String? {
        return tokenManager.meetingId.firstOrNull()
    }
}

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    name = name,
    role = UserRole.fromString(role),
    meetingId = meetingId,
    isActive = isActive,
)

fun User.toEntity() = UserEntity(
    id = id,
    username = username,
    name = name,
    role = role.value,
    meetingId = meetingId,
    isActive = isActive,
)
