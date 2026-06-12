package com.pay.eeaapp.ui.admin

import com.pay.eeaapp.data.dao.ProjectLocationRow

data class MapUiState(
    val locations: List<ProjectLocationRow> = emptyList(),
    val isLoading: Boolean = true
)