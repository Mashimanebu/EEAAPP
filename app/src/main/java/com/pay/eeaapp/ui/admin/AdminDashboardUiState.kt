package com.pay.eeaapp.ui.admin

import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.User

enum class AdminFilter { ALL, SUBMITTED, UNDER_REVIEW, AMENDMENTS_REQUIRED, APPROVED, REJECTED }

data class AdminDashboardUiState(
    val user: User? = null,
    val allProjects: List<Project> = emptyList(),
    val filter: AdminFilter = AdminFilter.ALL,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredProjects: List<Project>
        get() = when (filter) {
            AdminFilter.ALL -> allProjects
            AdminFilter.SUBMITTED -> allProjects.filter { it.status == ProjectStatus.SUBMITTED }
            AdminFilter.UNDER_REVIEW -> allProjects.filter { it.status == ProjectStatus.UNDER_REVIEW }
            AdminFilter.AMENDMENTS_REQUIRED -> allProjects.filter { it.status == ProjectStatus.AMENDMENTS_REQUIRED }
            AdminFilter.APPROVED -> allProjects.filter { it.status == ProjectStatus.APPROVED }
            AdminFilter.REJECTED -> allProjects.filter { it.status == ProjectStatus.REJECTED }
        }
}