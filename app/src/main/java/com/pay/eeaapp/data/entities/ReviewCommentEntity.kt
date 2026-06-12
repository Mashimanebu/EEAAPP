package com.pay.eeaapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_comments",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReviewCommentEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val officerUid: String,
    val comment: String,
    val createdAt: Long
)