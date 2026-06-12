package com.pay.eeaapp.domain.models

data class Project(
    val id: String,
    val proponentUid: String,
    val proponentName: String,
    val companyName: String,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val status: ProjectStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val documents: List<ProjectDocument> = emptyList(),
    val reviews: List<ReviewComment> = emptyList()
)