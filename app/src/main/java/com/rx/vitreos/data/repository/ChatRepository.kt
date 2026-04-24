package com.rx.vitreos.data.repository

import com.rx.vitreos.data.local.MessageDao
import com.rx.vitreos.data.local.MessageEntity
import com.rx.vitreos.data.local.UserDao
import com.rx.vitreos.data.local.UserEntity
import com.rx.vitreos.data.local.toDomain
import com.rx.vitreos.data.local.toEntity
import com.rx.vitreos.data.remote.SocketManager
import com.rx.vitreos.domain.model.Message
import com.rx.vitreos.domain.model.MessageStatus
import com.rx.vitreos.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val socketManager: SocketManager
) {
    fun getMessagesByRoom(roomId: String): Flow<List<Message>> {
        return messageDao.getMessagesByRoom(roomId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    suspend fun saveMessages(messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toEntity() })
    }

    suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    suspend fun saveUsers(users: List<User>) {
        userDao.insertUsers(users.map { it.toEntity() })
    }

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status.name)
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toDomain()
    }

    fun connect(userId: String) {
        socketManager.connect(userId)
    }

    fun disconnect() {
        socketManager.disconnect()
    }

    fun sendMessage(to: String, content: String) {
        socketManager.sendMessage(to, content)
    }

    fun sendTyping(to: String) {
        socketManager.sendTyping(to)
    }

    fun sendStopTyping(to: String) {
        socketManager.sendStopTyping(to)
    }

    fun joinRoom(withUser: String) {
        socketManager.joinRoom(withUser)
    }

    fun getHistory(withUser: String, callback: (List<Message>) -> Unit) {
        socketManager.getHistory(withUser) { jsonObjects ->
            val messages = jsonObjects.map { json ->
                Message(
                    id = json.optString("id", ""),
                    senderId = json.optString("senderId", ""),
                    receiverId = json.optString("receiverId", ""),
                    content = json.optString("content", ""),
                    roomId = json.optString("roomId", ""),
                    status = MessageStatus.valueOf(json.optString("status", "SENT")),
                    timestamp = json.optLong("createdAt", System.currentTimeMillis())
                )
            }
            callback(messages)
        }
    }

    fun checkOnlineStatus(userId: String) {
        socketManager.checkOnlineStatus(userId)
    }

    fun isConnected() = socketManager.isConnected

    fun typingUsers() = socketManager.typingUsers
}