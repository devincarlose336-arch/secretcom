package com.secretcom.app.data.repository

import com.secretcom.app.data.remote.api.ApiService
import com.secretcom.app.data.remote.api.GenerateIdsRequest
import com.secretcom.app.domain.model.*
import com.secretcom.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDashboard(): Resource<DashboardData> {
        return try {
            val response = apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val dashboard = DashboardData(
                    rooms = body.rooms.map {
                        RoomStats(it.roomNumber, it.name, it.participantCount, it.maxParticipants, it.isFull)
                    },
                    meetingIds = MeetingIdStats(
                        body.meetingIds.total, body.meetingIds.assigned, body.meetingIds.available
                    ),
                    totalUsers = body.users.total,
                    totalAdmins = body.users.admins,
                )
                Resource.Success(dashboard)
            } else {
                Resource.Error("Failed to fetch dashboard")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch dashboard")
        }
    }

    suspend fun getUsers(): Resource<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.users.map { it.toDomain() })
            } else {
                Resource.Error("Failed to fetch users")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch users")
        }
    }

    suspend fun getAdmins(): Resource<List<User>> {
        return try {
            val response = apiService.getAdmins()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.admins.map { it.toDomain() })
            } else {
                Resource.Error("Failed to fetch admins")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch admins")
        }
    }

    suspend fun toggleUserStatus(userId: String): Resource<User> {
        return try {
            val response = apiService.toggleUserStatus(userId)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.user.toDomain())
            } else {
                Resource.Error("Failed to toggle user status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to toggle user status")
        }
    }

    suspend fun deleteUser(userId: String): Resource<String> {
        return try {
            val response = apiService.deleteUser(userId)
            if (response.isSuccessful) {
                Resource.Success("User deleted")
            } else {
                Resource.Error("Failed to delete user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete user")
        }
    }

    suspend fun generateMeetingIds(count: Int = 2000): Resource<String> {
        return try {
            val response = apiService.generateMeetingIds(GenerateIdsRequest(count))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Resource.Success("Generated ${body.generated} IDs (Total: ${body.total})")
            } else {
                Resource.Error("Failed to generate meeting IDs")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to generate meeting IDs")
        }
    }

    suspend fun getMeetingIdStats(): Resource<MeetingIdStats> {
        return try {
            val response = apiService.getMeetingIdStats()
            if (response.isSuccessful && response.body() != null) {
                val s = response.body()!!.stats
                Resource.Success(MeetingIdStats(s.total, s.assigned, s.available))
            } else {
                Resource.Error("Failed to fetch stats")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch stats")
        }
    }

    suspend fun getIceServers(): Resource<List<IceServer>> {
        return try {
            val response = apiService.getIceServers()
            if (response.isSuccessful && response.body() != null) {
                val servers = response.body()!!.iceServers.map {
                    IceServer(it.urls, it.username, it.credential)
                }
                Resource.Success(servers)
            } else {
                Resource.Error("Failed to fetch ICE servers")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch ICE servers")
        }
    }
}
