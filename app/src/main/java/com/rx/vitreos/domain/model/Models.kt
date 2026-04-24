package com.rx.vitreos.domain.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val roomId: String = "",
    val status: MessageStatus = MessageStatus.SENT,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

data class User(
    val userId: String = "",
    val username: String = "",
    val phone: String = "",
    val isOnline: Boolean = false
)

data class Contact(
    val user: User,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0
)