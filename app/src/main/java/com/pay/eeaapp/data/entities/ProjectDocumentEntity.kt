package com.pay.eeaapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "project_documents",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectDocumentEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val fileName: String,
    val fileUrl: String,
    val uploadedByUid: String,
    val uploadedByRole: String,
    val uploadedAt: Long
)