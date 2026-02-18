package com.secretcom.app.ui.meeting

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secretcom.app.BuildConfig
import com.secretcom.app.data.local.dao.RecordingDao
import com.secretcom.app.data.local.entity.RecordingEntity
import com.secretcom.app.data.remote.socket.SocketManager
import com.secretcom.app.data.remote.webrtc.WebRTCManager
import com.secretcom.app.data.repository.AdminRepository
import com.secretcom.app.data.repository.RoomRepository
import com.secretcom.app.domain.model.IceServer
import com.secretcom.app.domain.model.Participant
import com.secretcom.app.domain.model.Room
import com.secretcom.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

data class MeetingState(
    val isLoading: Boolean = false,
    val rooms: List<Room> = emptyList(),
    val currentRoom: Room? = null,
    val participants: List<Participant> = emptyList(),
    val isSpeaking: Boolean = false,
    val isRecording: Boolean = false,
    val isConnected: Boolean = false,
    val error: String? = null,
    val speakingUsers: Set<String> = emptySet(),
)

@HiltViewModel
class MeetingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val roomRepository: RoomRepository,
    private val adminRepository: AdminRepository,
    private val socketManager: SocketManager,
    private val webRTCManager: WebRTCManager,
    private val recordingDao: RecordingDao
) : ViewModel() {

    private val _state = MutableStateFlow(MeetingState())
    val state: StateFlow<MeetingState> = _state

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null

    init {
        observeSocket()
    }

    fun loadRooms() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = roomRepository.getRooms()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, rooms = result.data) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun connectSocket() {
        socketManager.connect(BuildConfig.SOCKET_URL)
        viewModelScope.launch {
            socketManager.connectionState.collect { connectionState ->
                _state.update {
                    it.copy(isConnected = connectionState == SocketManager.ConnectionState.CONNECTED)
                }
            }
        }
    }

    fun initializeWebRTC() {
        viewModelScope.launch {
            when (val result = adminRepository.getIceServers()) {
                is Resource.Success -> webRTCManager.initialize(result.data)
                is Resource.Error -> {
                    val defaultServers = listOf(
                        IceServer("stun:stun.l.google.com:19302"),
                        IceServer("stun:stun1.l.google.com:19302"),
                        IceServer("stun:stun2.l.google.com:19302"),
                    )
                    webRTCManager.initialize(defaultServers)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun joinRoom(roomNumber: Int, meetingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = roomRepository.joinRoom(roomNumber)) {
                is Resource.Success -> {
                    val room = result.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentRoom = room,
                            participants = room.participants,
                        )
                    }
                    webRTCManager.setCurrentRoom(roomNumber)
                    socketManager.emit("join-room", JSONObject().apply {
                        put("roomNumber", roomNumber)
                        put("meetingId", meetingId)
                    })
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun leaveRoom() {
        val room = _state.value.currentRoom ?: return
        viewModelScope.launch {
            socketManager.emit("leave-room", JSONObject().apply {
                put("roomNumber", room.roomNumber)
            })
            roomRepository.leaveRoom(room.roomNumber)
            webRTCManager.closeAllConnections()
            _state.update {
                it.copy(currentRoom = null, participants = emptyList(), speakingUsers = emptySet())
            }
        }
    }

    fun startSpeaking() {
        webRTCManager.startSpeaking()
        _state.update { it.copy(isSpeaking = true) }
    }

    fun stopSpeaking() {
        webRTCManager.stopSpeaking()
        _state.update { it.copy(isSpeaking = false) }
    }

    fun muteParticipant(meetingId: String, muted: Boolean) {
        val room = _state.value.currentRoom ?: return
        viewModelScope.launch {
            roomRepository.muteParticipant(room.roomNumber, meetingId, muted)
            socketManager.emit("admin-mute", JSONObject().apply {
                put("roomNumber", room.roomNumber)
                put("meetingId", meetingId)
                put("muted", muted)
            })
        }
    }

    fun removeParticipant(meetingId: String) {
        val room = _state.value.currentRoom ?: return
        viewModelScope.launch {
            roomRepository.removeParticipant(room.roomNumber, meetingId)
            socketManager.emit("admin-remove", JSONObject().apply {
                put("roomNumber", room.roomNumber)
                put("meetingId", meetingId)
            })
        }
    }

    fun monitorRoom(roomNumber: Int) {
        socketManager.emit("admin-monitor", JSONObject().apply {
            put("roomNumber", roomNumber)
        })
    }

    @Suppress("DEPRECATION")
    fun startRecording() {
        val room = _state.value.currentRoom ?: return
        try {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                "secretcom_recordings"
            )
            if (!dir.exists()) dir.mkdirs()

            recordingFile = File(dir, "recording_room${room.roomNumber}_${System.currentTimeMillis()}.3gp")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFile?.absolutePath)
                prepare()
                start()
            }
            _state.update { it.copy(isRecording = true) }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to start recording: ${e.message}") }
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            recordingFile?.let { file ->
                val room = _state.value.currentRoom
                viewModelScope.launch {
                    recordingDao.insertRecording(
                        RecordingEntity(
                            roomNumber = room?.roomNumber ?: 0,
                            roomName = room?.name ?: "Unknown",
                            filePath = file.absolutePath,
                            fileSize = file.length(),
                        )
                    )
                }
            }
            _state.update { it.copy(isRecording = false) }
        } catch (e: Exception) {
            _state.update { it.copy(error = "Failed to stop recording: ${e.message}") }
        }
    }

    private fun observeSocket() {
        socketManager.on("room-joined") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val participantsArray = data.getJSONArray("participants")
                val participants = parseParticipants(participantsArray)
                _state.update { it.copy(participants = participants) }
            }
        }

        socketManager.on("participant-joined") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val pData = data.getJSONObject("participant")
                val participant = Participant(
                    meetingId = pData.optString("meetingId"),
                    name = pData.optString("name"),
                    socketId = pData.optString("socketId"),
                )
                _state.update { it.copy(participants = it.participants + participant) }
            }
        }

        socketManager.on("participant-left") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val meetingId = data.getString("meetingId")
                _state.update {
                    it.copy(participants = it.participants.filter { p -> p.meetingId != meetingId })
                }
                webRTCManager.removePeerConnection(meetingId)
            }
        }

        socketManager.on("participant-muted") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val meetingId = data.getString("meetingId")
                val muted = data.getBoolean("muted")
                _state.update {
                    it.copy(participants = it.participants.map { p ->
                        if (p.meetingId == meetingId) p.copy(isMuted = muted) else p
                    })
                }
            }
        }

        socketManager.on("participant-removed") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val meetingId = data.getString("meetingId")
                _state.update {
                    it.copy(participants = it.participants.filter { p -> p.meetingId != meetingId })
                }
            }
        }

        socketManager.on("user-speaking") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val meetingId = data.getString("meetingId")
                val speaking = data.getBoolean("speaking")
                _state.update {
                    val newSpeaking = it.speakingUsers.toMutableSet()
                    if (speaking) newSpeaking.add(meetingId) else newSpeaking.remove(meetingId)
                    it.copy(speakingUsers = newSpeaking)
                }
            }
        }

        socketManager.on("participant-count") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val roomNumber = data.getInt("roomNumber")
                val count = data.getInt("count")
                _state.update {
                    it.copy(rooms = it.rooms.map { room ->
                        if (room.roomNumber == roomNumber) {
                            room.copy(participants = List(count) { Participant() })
                        } else room
                    })
                }
            }
        }
    }

    private fun parseParticipants(array: JSONArray): List<Participant> {
        val list = mutableListOf<Participant>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Participant(
                    userId = obj.optString("userId"),
                    meetingId = obj.optString("meetingId"),
                    name = obj.optString("name"),
                    socketId = obj.optString("socketId"),
                    isMuted = obj.optBoolean("isMuted", false),
                )
            )
        }
        return list
    }

    fun disconnect() {
        if (_state.value.isRecording) stopRecording()
        leaveRoom()
        socketManager.disconnect()
        webRTCManager.dispose()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
