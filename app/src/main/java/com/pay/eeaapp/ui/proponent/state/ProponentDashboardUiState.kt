package com.pay.eeaapp.ui.proponent.state

import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.User

data class ProponentDashboardUiState(
    val user: User? = null,
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)