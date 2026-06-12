package com.pay.eeaapp.domain.models

data class ProjectStats(
    val totalSubmitted: Int,
    val totalApproved: Int,
    val totalRejected: Int,
    val totalUnderReview: Int,
    val weekly: Map<String, Int>,
    val monthly: Map<String, Int>,
    val yearly: Map<String, Int>
)