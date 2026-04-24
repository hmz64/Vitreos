package com.rx.vitreos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rx.vitreos.domain.model.MessageStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val roomId: String,
    val status: String,
    val timestamp: Long
)

fun MessageEntity.toDomain() = com.rx.vitreos.domain.model.Message(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    content = content,
    roomId = roomId,
    status = MessageStatus.valueOf(status),
    timestamp = timestamp
)

fun com.rx.vitreos.domain.model.Message.toEntity() = MessageEntity(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    content = content,
    roomId = roomId,
    status = status.name,
    timestamp = timestamp
)