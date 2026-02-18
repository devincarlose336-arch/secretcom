package com.secretcom.app.data.repository

import com.secretcom.app.data.remote.api.*
import com.secretcom.app.domain.model.Participant
import com.secretcom.app.domain.model.Room
import com.secretcom.app.domain.model.RoomStats
import com.secretcom.app.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getRooms(): Resource<List<Room>> {
        return try {
            val response = apiService.getRooms()
            if (response.isSuccessful && response.body() != null) {
                val rooms = response.body()!!.rooms.map { it.toDomain() }
                Resource.Success(rooms)
            } else {
                Resource.Error("Failed to fetch rooms")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch rooms")
        }
    }

    suspend fun getRoom(roomNumber: Int): Resource<Room> {
        return try {
            val response = apiService.getRoom(roomNumber)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.room.toDomain())
            } else {
                Resource.Error("Failed to fetch room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch room")
        }
    }

    suspend fun joinRoom(roomNumber: Int): Resource<Room> {
        return try {
            val response = apiService.joinRoom(roomNumber)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.room.toDomain())
            } else {
                Resource.Error("Failed to join room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to join room")
        }
    }

    suspend fun leaveRoom(roomNumber: Int): Resource<Room> {
        return try {
            val response = apiService.leaveRoom(roomNumber)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.room.toDomain())
            } else {
                Resource.Error("Failed to leave room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to leave room")
        }
    }

    suspend fun muteParticipant(roomNumber: Int, meetingId: String, muted: Boolean): Resource<Room> {
        return try {
            val response = apiService.muteParticipant(roomNumber, MuteRequest(meetingId, muted))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.room.toDomain())
            } else {
                Resource.Error("Failed to mute participant")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mute participant")
        }
    }

    suspend fun removeParticipant(roomNumber: Int, meetingId: String): Resource<Room> {
        return try {
            val response = apiService.removeParticipant(roomNumber, RemoveRequest(meetingId))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!.room.toDomain())
            } else {
                Resource.Error("Failed to remove participant")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove participant")
        }
    }

    suspend fun getRoomStats(): Resource<List<RoomStats>> {
        return try {
            val response = apiService.getRoomStats()
            if (response.isSuccessful && response.body() != null) {
                val stats = response.body()!!.stats.map {
                    RoomStats(it.roomNumber, it.name, it.participantCount, it.maxParticipants, it.isFull)
                }
                Resource.Success(stats)
            } else {
                Resource.Error("Failed to fetch room stats")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch room stats")
        }
    }
}

fun RoomDto.toDomain() = Room(
    roomNumber = roomNumber,
    name = name,
    participants = participants.map { it.toDomain() },
    maxParticipants = maxParticipants,
    isActive = isActive,
)

fun ParticipantDto.toDomain() = Participant(
    userId = userId,
    meetingId = meetingId,
    name = name,
    socketId = socketId,
    isMuted = isMuted,
)
