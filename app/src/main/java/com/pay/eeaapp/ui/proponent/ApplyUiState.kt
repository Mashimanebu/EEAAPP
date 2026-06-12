package com.pay.eeaapp.ui.proponent

import android.net.Uri

data class ApplyUiState(
    val title: String = "",
    val description: String = "",
    val companyName: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val pickedFiles: List<Pair<Uri, String>> = emptyList(),  // uri to filename
    val submitState: SubmitState = SubmitState.Idle
)