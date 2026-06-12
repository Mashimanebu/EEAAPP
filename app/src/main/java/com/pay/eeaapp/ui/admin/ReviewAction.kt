package com.pay.eeaapp.ui.admin

sealed class ReviewAction {
    object Idle    : ReviewAction()
    object Loading : ReviewAction()
    object Done    : ReviewAction()
    data class Error(val message: String) : ReviewAction()
}
