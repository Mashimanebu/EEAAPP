package com.pay.eeaapp.domain.models

data class ReviewComment(
    val id: String,
    val projectId: String,
    val officerUid: String,
    val comment: String,
    val createdAt: Long
)