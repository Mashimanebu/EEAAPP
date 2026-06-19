package com.pay.eeaapp.ui.admin.state

import com.pay.eeaapp.data.dao.ProjectLocationRow

data class MapUiState(
    val locations: List<ProjectLocationRow> = emptyList(),
    val isLoading: Boolean = true
)