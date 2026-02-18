package com.secretcom.app.data.remote.webrtc

import android.content.Context
import com.secretcom.app.data.remote.socket.SocketManager
import com.secretcom.app.domain.model.IceServer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socketManager: SocketManager
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private var localAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null
    private var eglBase: EglBase? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private var currentRoomNumber: Int? = null
    private var iceServers: List<PeerConnection.IceServer> = emptyList()

    fun initialize(servers: List<IceServer>) {
        iceServers = servers.map { server ->
            val builder = PeerConnection.IceServer.builder(server.urls)
            server.username?.let { builder.setUsername(it) }
            server.credential?.let { builder.setPassword(it) }
            builder.createIceServer()
        }

        eglBase = EglBase.create()

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )

        val encoderFactory = DefaultVideoEncoderFactory(eglBase!!.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase!!.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(
                JavaAudioDeviceModule.builder(context)
                    .setUseHardwareAcousticEchoCanceler(true)
                    .setUseHardwareNoiseSuppressor(true)
                    .createAudioDeviceModule()
            )
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        createLocalAudioTrack()
        setupSocketListeners()
    }

    private fun createLocalAudioTrack() {
        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_local", audioSource)
        localAudioTrack?.setEnabled(false)
    }

    fun createPeerConnection(remoteSocketId: String): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val observer = object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}

            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    val data = JSONObject().apply {
                        put("targetSocketId", remoteSocketId)
                        put("candidate", JSONObject().apply {
                            put("sdp", it.sdp)
                            put("sdpMLineIndex", it.sdpMLineIndex)
                            put("sdpMid", it.sdpMid)
                        })
                    }
                    socketManager.emit("webrtc-ice-candidate", data)
                }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(dc: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
        }

        val peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
        localAudioTrack?.let { track ->
            peerConnection?.addTrack(track, listOf("stream_local"))
        }

        peerConnection?.let { peerConnections[remoteSocketId] = it }
        return peerConnection
    }

    fun createOffer(remoteSocketId: String) {
        val pc = peerConnections[remoteSocketId] ?: createPeerConnection(remoteSocketId) ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    pc.setLocalDescription(SimpleSdpObserver(), it)
                    val data = JSONObject().apply {
                        put("targetSocketId", remoteSocketId)
                        put("offer", JSONObject().apply {
                            put("type", it.type.canonicalForm())
                            put("sdp", it.description)
                        })
                    }
                    socketManager.emit("webrtc-offer", data)
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun handleOffer(fromSocketId: String, offerSdp: String) {
        val pc = peerConnections[fromSocketId] ?: createPeerConnection(fromSocketId) ?: return
        val sdp = SessionDescription(SessionDescription.Type.OFFER, offerSdp)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
                }
                pc.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(answerSdp: SessionDescription?) {
                        answerSdp?.let {
                            pc.setLocalDescription(SimpleSdpObserver(), it)
                            val data = JSONObject().apply {
                                put("targetSocketId", fromSocketId)
                                put("answer", JSONObject().apply {
                                    put("type", it.type.canonicalForm())
                                    put("sdp", it.description)
                                })
                            }
                            socketManager.emit("webrtc-answer", data)
                        }
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(error: String?) {}
                    override fun onSetFailure(error: String?) {}
                }, constraints)
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, sdp)
    }

    private fun handleAnswer(fromSocketId: String, answerSdp: String) {
        val pc = peerConnections[fromSocketId] ?: return
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
        pc.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    private fun handleIceCandidate(fromSocketId: String, candidate: JSONObject) {
        val pc = peerConnections[fromSocketId] ?: return
        val iceCandidate = IceCandidate(
            candidate.optString("sdpMid"),
            candidate.optInt("sdpMLineIndex"),
            candidate.optString("sdp")
        )
        pc.addIceCandidate(iceCandidate)
    }

    private fun setupSocketListeners() {
        socketManager.on("webrtc-offer") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val fromSocketId = data.getString("from")
                val offer = data.getJSONObject("offer")
                handleOffer(fromSocketId, offer.getString("sdp"))
            }
        }

        socketManager.on("webrtc-answer") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val fromSocketId = data.getString("from")
                val answer = data.getJSONObject("answer")
                handleAnswer(fromSocketId, answer.getString("sdp"))
            }
        }

        socketManager.on("webrtc-ice-candidate") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val fromSocketId = data.getString("from")
                val candidate = data.getJSONObject("candidate")
                handleIceCandidate(fromSocketId, candidate)
            }
        }

        socketManager.on("participant-joined") { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val participant = data.getJSONObject("participant")
                val socketId = participant.getString("socketId")
                createOffer(socketId)
            }
        }
    }

    fun startSpeaking() {
        localAudioTrack?.setEnabled(true)
        _isSpeaking.value = true
        currentRoomNumber?.let { room ->
            socketManager.emit("push-to-talk-start", JSONObject().apply {
                put("roomNumber", room)
            })
        }
    }

    fun stopSpeaking() {
        localAudioTrack?.setEnabled(false)
        _isSpeaking.value = false
        currentRoomNumber?.let { room ->
            socketManager.emit("push-to-talk-end", JSONObject().apply {
                put("roomNumber", room)
            })
        }
    }

    fun setCurrentRoom(roomNumber: Int) {
        currentRoomNumber = roomNumber
    }

    fun removePeerConnection(socketId: String) {
        peerConnections[socketId]?.close()
        peerConnections.remove(socketId)
    }

    fun closeAllConnections() {
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        currentRoomNumber = null
    }

    fun dispose() {
        closeAllConnections()
        localAudioTrack?.dispose()
        audioSource?.dispose()
        peerConnectionFactory?.dispose()
        eglBase?.release()
    }

    private class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {}
        override fun onSetFailure(error: String?) {}
    }
}
