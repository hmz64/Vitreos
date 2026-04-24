package com.rx.vitreos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rx.vitreos.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val phone: String,
    val isOnline: Boolean = false
)

fun UserEntity.toDomain() = User(
    userId = userId,
    username = username,
    phone = phone,
    isOnline = isOnline
)

fun User.toEntity() = UserEntity(
    userId = userId,
    username = username,
    phone = phone,
    isOnline = isOnline
)