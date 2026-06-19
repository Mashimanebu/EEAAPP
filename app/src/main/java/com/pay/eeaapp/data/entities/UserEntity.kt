package com.pay.eeaapp.data.entities

import androidx.room.Entity

import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String = "",
    val fullName: String? = null,
    val email: String? = null,
    val company: String? = null,
    val role: String? = null
)





