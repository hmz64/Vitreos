package com.rx.vitreos.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {
    
    companion object {
        private const val SOCKET_URL = "http://10.0.2.2:3001"
        private const val NAMESPACE = "/chat"
    }

    private var mSocket: Socket? = null
    private var currentUserId: String? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow<List<JSONObject>>(emptyList())
    val messages: StateFlow<List<JSONObject>> = _messages.asStateFlow()

    private val _typingUsers = MutableStateFlow<Set<String>>(emptySet())
    val typingUsers: StateFlow<Set<String>> = _typingUsers.asStateFlow()

    private val _onlineStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val onlineStatus: StateFlow<Map<String, Boolean>> = _onlineStatus.asStateFlow()

    @Synchronized
    fun connect(userId: String) {
        if (mSocket?.connected() == true && currentUserId == userId) {
            return
        }

        disconnect()
        currentUserId = userId

        try {
            val options = IO.Options().apply {
                query = mapOf("userId" to userId)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 5000
            }

            mSocket = IO.socket(URI.create("$SOCKET_URL$NAMESPACE"), options)
            setupEventListeners()
            mSocket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEventListeners() {
        mSocket?.apply {
            on(Socket.EVENT_CONNECT) {
                _isConnected.value = true
                println("Socket connected")
            }

            on(Socket.EVENT_DISCONNECT) {
                _isConnected.value = false
                println("Socket disconnected")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                println("Socket connection error: ${args.joinToString()}")
            }

            on("receive_message") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    _messages.value = _messages.value + data
                }
            }

            on("message_ack") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val messageId = data.getString("messageId")
                    println("Message acknowledged: $messageId")
                }
            }

            on("user_typing") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val fromUser = data.getString("from")
                    _typingUsers.value = _typingUsers.value + fromUser
                }
            }

            on("user_stop_typing") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val fromUser = data.getString("from")
                    _typingUsers.value = _typingUsers.value - fromUser
                }
            }

            on("get_online_status") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val userId = data.getString("userId")
                    val isOnline = data.getBoolean("isOnline")
                    _onlineStatus.value = _onlineStatus.value + (userId to isOnline)
                }
            }
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
        mSocket?.off()
        mSocket = null
        currentUserId = null
        _isConnected.value = false
    }

    fun sendMessage(to: String, content: String) {
        val json = JSONObject().apply {
            put("to", to)
            put("content", content)
        }
        mSocket?.emit("private_message", json)
    }

    fun sendTyping(to: String) {
        val json = JSONObject().apply {
            put("to", to)
        }
        mSocket?.emit("typing", json)
    }

    fun sendStopTyping(to: String) {
        val json = JSONObject().apply {
            put("to", to)
        }
        mSocket?.emit("stop_typing", json)
    }

    fun joinRoom(withUser: String) {
        val json = JSONObject().apply {
            put("withUser", withUser)
        }
        mSocket?.emit("join_room", json)
    }

    fun getHistory(withUser: String, callback: (List<JSONObject>) -> Unit) {
        val json = JSONObject().apply {
            put("withUser", withUser)
        }
        
        mSocket?.emit("get_history", json) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                val messagesArray = data.getJSONArray("messages")
                val messages = mutableListOf<JSONObject>()
                for (i in 0 until messagesArray.length()) {
                    messages.add(messagesArray.getJSONObject(i))
                }
                callback(messages)
            } else {
                callback(emptyList())
            }
        }
    }

    fun checkOnlineStatus(userId: String) {
        val json = JSONObject().apply {
            put("userId", userId)
        }
        mSocket?.emit("get_online_status", json)
    }

    fun sendHeartbeat() {
        mSocket?.emit("heartbeat")
    }
}