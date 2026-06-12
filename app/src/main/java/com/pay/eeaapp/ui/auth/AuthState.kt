package com.pay.eeaapp.ui.auth

import com.pay.eeaapp.domain.models.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}