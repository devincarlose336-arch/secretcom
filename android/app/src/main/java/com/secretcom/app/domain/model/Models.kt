package com.secretcom.app.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val meetingId: String? = null,
    val isActive: Boolean = true,
)

enum class UserRole(val value: String) {
    SUPER_ADMIN("super_admin"),
    ADMIN("admin"),
    USER("user");

    companion object {
        fun fromString(value: String): UserRole = entries.find { it.value == value } ?: USER
    }
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)

data class Room(
    val roomNumber: Int,
    val name: String,
    val participants: List<Participant> = emptyList(),
    val maxParticipants: Int = 25,
    val isActive: Boolean = true,
)

data class Participant(
    val userId: String = "",
    val meetingId: String = "",
    val name: String = "",
    val socketId: String = "",
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
)

data class MeetingIdStats(
    val total: Int = 0,
    val assigned: Int = 0,
    val available: Int = 0,
)

data class RoomStats(
    val roomNumber: Int,
    val name: String,
    val participantCount: Int,
    val maxParticipants: Int,
    val isFull: Boolean,
)

data class DashboardData(
    val rooms: List<RoomStats> = emptyList(),
    val meetingIds: MeetingIdStats = MeetingIdStats(),
    val totalUsers: Int = 0,
    val totalAdmins: Int = 0,
)

data class IceServer(
    val urls: String,
    val username: String? = null,
    val credential: String? = null,
)
