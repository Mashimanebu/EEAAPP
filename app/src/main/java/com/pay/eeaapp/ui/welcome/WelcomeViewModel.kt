package com.pay.eeaapp.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
class WelcomeViewModel : ViewModel() {

    val uiState: StateFlow<WelcomeUiState> = BannerRepository.banners
        .map { WelcomeUiState(banners = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WelcomeUiState(isLoading = true)
        )
}

