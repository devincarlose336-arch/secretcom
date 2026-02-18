package com.secretcom.app.data.remote.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val name: String, val meetingId: String)
data class CreateAdminRequest(val username: String, val password: String, val name: String)
data class RefreshTokenRequest(val refreshToken: String)
data class MuteRequest(val meetingId: String, val muted: Boolean)
data class RemoveRequest(val meetingId: String)
data class GenerateIdsRequest(val count: Int = 2000)

data class UserDto(
    @SerializedName("_id") val id: String = "",
    val username: String = "",
    val name: String = "",
    val role: String = "user",
    val meetingId: String? = null,
    val isActive: Boolean = true,
)

data class AuthResponseDto(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
)

data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
)

data class ProfileResponseDto(val user: UserDto)

data class ParticipantDto(
    val userId: String = "",
    val meetingId: String = "",
    val name: String = "",
    val socketId: String = "",
    val isMuted: Boolean = false,
)

data class RoomDto(
    val roomNumber: Int,
    val name: String,
    val participants: List<ParticipantDto> = emptyList(),
    val maxParticipants: Int = 25,
    val isActive: Boolean = true,
)

data class RoomsResponseDto(val rooms: List<RoomDto>)
data class RoomResponseDto(val room: RoomDto, val message: String? = null)

data class RoomStatsDto(
    val roomNumber: Int,
    val name: String,
    val participantCount: Int,
    val maxParticipants: Int,
    val isFull: Boolean,
)
data class RoomStatsResponseDto(val stats: List<RoomStatsDto>)

data class MeetingIdStatsDto(val total: Int, val assigned: Int, val available: Int)
data class MeetingIdStatsResponseDto(val stats: MeetingIdStatsDto)
data class MeetingIdValidateDto(val valid: Boolean, val isAssigned: Boolean = false, val meetingId: String = "")
data class GenerateIdsResponseDto(val message: String, val generated: Int, val total: Int)

data class DashboardRoomsDto(val rooms: List<RoomStatsDto>)
data class DashboardUsersDto(val total: Int, val admins: Int)
data class DashboardResponseDto(
    val rooms: List<RoomStatsDto>,
    val meetingIds: MeetingIdStatsDto,
    val users: DashboardUsersDto,
)

data class UsersResponseDto(val users: List<UserDto>)
data class AdminsResponseDto(val admins: List<UserDto>)
data class MessageResponseDto(val message: String)

data class IceServerDto(val urls: String, val username: String? = null, val credential: String? = null)
data class IceServersResponseDto(val iceServers: List<IceServerDto>)

data class UserToggleResponseDto(val user: UserDto, val message: String)

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponseDto>

    @POST("api/auth/admin/create")
    suspend fun createAdmin(@Body request: CreateAdminRequest): Response<MessageResponseDto>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponseDto>

    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ProfileResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    @GET("api/rooms")
    suspend fun getRooms(): Response<RoomsResponseDto>

    @GET("api/rooms/stats")
    suspend fun getRoomStats(): Response<RoomStatsResponseDto>

    @GET("api/rooms/{roomNumber}")
    suspend fun getRoom(@Path("roomNumber") roomNumber: Int): Response<RoomResponseDto>

    @POST("api/rooms/{roomNumber}/join")
    suspend fun joinRoom(@Path("roomNumber") roomNumber: Int): Response<RoomResponseDto>

    @POST("api/rooms/{roomNumber}/leave")
    suspend fun leaveRoom(@Path("roomNumber") roomNumber: Int): Response<RoomResponseDto>

    @POST("api/rooms/{roomNumber}/mute")
    suspend fun muteParticipant(
        @Path("roomNumber") roomNumber: Int,
        @Body request: MuteRequest
    ): Response<RoomResponseDto>

    @POST("api/rooms/{roomNumber}/remove")
    suspend fun removeParticipant(
        @Path("roomNumber") roomNumber: Int,
        @Body request: RemoveRequest
    ): Response<RoomResponseDto>

    @POST("api/meeting-ids/generate")
    suspend fun generateMeetingIds(@Body request: GenerateIdsRequest): Response<GenerateIdsResponseDto>

    @GET("api/meeting-ids/stats")
    suspend fun getMeetingIdStats(): Response<MeetingIdStatsResponseDto>

    @GET("api/meeting-ids/validate/{meetingId}")
    suspend fun validateMeetingId(@Path("meetingId") meetingId: String): Response<MeetingIdValidateDto>

    @GET("api/admin/dashboard")
    suspend fun getDashboard(): Response<DashboardResponseDto>

    @GET("api/admin/users")
    suspend fun getUsers(): Response<UsersResponseDto>

    @GET("api/admin/admins")
    suspend fun getAdmins(): Response<AdminsResponseDto>

    @PATCH("api/admin/users/{userId}/toggle")
    suspend fun toggleUserStatus(@Path("userId") userId: String): Response<UserToggleResponseDto>

    @DELETE("api/admin/users/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): Response<MessageResponseDto>

    @GET("api/webrtc/ice-servers")
    suspend fun getIceServers(): Response<IceServersResponseDto>
}
