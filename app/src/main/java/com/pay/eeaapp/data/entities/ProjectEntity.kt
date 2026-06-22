package com.pay.eeaapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = "",
    val proponentUid: String = "",
    val proponentName: String = "",
    val companyName: String = "",
    val title: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)