package com.secretcom.app.data.remote.socket

import com.secretcom.app.data.preferences.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private var socket: Socket? = null
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    enum class ConnectionState {
        CONNECTED, CONNECTING, DISCONNECTED, ERROR
    }

    fun connect(baseUrl: String) {
        if (socket?.connected() == true) return

        _connectionState.value = ConnectionState.CONNECTING
        val token = runBlocking { tokenManager.accessToken.firstOrNull() } ?: return

        val options = IO.Options().apply {
            auth = mapOf("token" to token)
            forceNew = true
            reconnection = true
            reconnectionAttempts = 10
            reconnectionDelay = 1000
            timeout = 20000
        }

        socket = IO.socket("$baseUrl/rooms", options).apply {
            on(Socket.EVENT_CONNECT) {
                _connectionState.value = ConnectionState.CONNECTED
            }
            on(Socket.EVENT_DISCONNECT) {
                _connectionState.value = ConnectionState.DISCONNECTED
            }
            on(Socket.EVENT_CONNECT_ERROR) {
                _connectionState.value = ConnectionState.ERROR
            }
            connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }

    fun on(event: String, listener: (Array<Any>) -> Unit) {
        socket?.on(event) { args -> listener(args) }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun isConnected(): Boolean = socket?.connected() == true

    fun getSocketId(): String? = socket?.id()
}
