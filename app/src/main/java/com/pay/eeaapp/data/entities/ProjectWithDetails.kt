package com.pay.eeaapp.data.entities

import androidx.room.Embedded
import androidx.room.Relation

data class ProjectWithDetails(
    @Embedded val project: ProjectEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val documents: List<ProjectDocumentEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val reviews: List<ReviewCommentEntity>
)