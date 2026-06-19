package com.pay.eeaapp.ui.proponent.state

import android.net.Uri

data class ApplyUiState(
    val title: String = "",
    val description: String = "",
    val companyName: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val pickedFiles: List<Pair<Uri, String>> = emptyList(),
    val submitState: SubmitState = SubmitState.Idle
)