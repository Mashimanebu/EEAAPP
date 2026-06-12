package com.pay.eeaapp.domain.models

data class ProjectDocument(
    val id: String,
    val projectId: String,
    val fileName: String,
    val fileUrl: String,
    val uploadedByUid: String,
    val uploadedByRole: UserRole,
    val uploadedAt: Long
)