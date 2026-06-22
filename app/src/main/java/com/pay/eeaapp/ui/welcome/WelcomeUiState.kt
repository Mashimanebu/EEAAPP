package com.pay.eeaapp.ui.welcome

data class WelcomeUiState(
    val banners: List<BannerImage> = emptyList(),
    val isLoading: Boolean = false
)