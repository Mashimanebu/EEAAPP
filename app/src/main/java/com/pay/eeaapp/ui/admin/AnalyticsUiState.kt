package com.pay.eeaapp.ui.admin

import com.pay.eeaapp.domain.models.ProjectStats

enum class AnalyticsPeriod { WEEKLY, MONTHLY, YEARLY }

data class AnalyticsUiState(
    val stats: ProjectStats? = null,
    val period: AnalyticsPeriod = AnalyticsPeriod.MONTHLY,
    val isLoading: Boolean = true,
    val error: String? = null
)