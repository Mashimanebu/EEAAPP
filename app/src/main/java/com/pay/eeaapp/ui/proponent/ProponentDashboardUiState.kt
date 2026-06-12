package com.pay.eeaapp.ui.proponent

import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.User

data class ProponentDashboardUiState(
    val user: User? = null,
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)