package com.pay.eeaapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pay.eeaapp.di.ServiceLocator
import com.pay.eeaapp.ui.auth.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val signIn = ServiceLocator.signInUseCase(app)
    private val getSession = ServiceLocator.getSessionUseCase(app)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun checkSession() {
        viewModelScope.launch {
            val user = getSession.currentUser()
            if (user != null) _state.value = AuthState.Success(user)
        }
    }

    fun login(email: String , password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Email and password are required.")
            return
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            _state.value = try {
                AuthState.Success(signIn(email.trim(), password))
            } catch (e: Exception) {
                AuthState.Error(e.message ?: "Login failed. Please try again.")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}


class SignUpViewModel(app: Application) : AndroidViewModel(app) {

    private val signUp = ServiceLocator.signUpUseCase(app)

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun signUp(fullName: String, company: String, email: String, password: String, confirm: String) {
        when {
            fullName.isBlank() -> { _state.value = AuthState.Error("Full name is required."); return }
            email.isBlank()    -> { _state.value = AuthState.Error("Email is required."); return }
            password.length < 6 -> { _state.value = AuthState.Error("Password must be at least 6 characters."); return }
            password != confirm  -> { _state.value = AuthState.Error("Passwords do not match."); return }
        }
        viewModelScope.launch {
            _state.value = AuthState.Loading
            _state.value = try {
                AuthState.Success(signUp(fullName.trim(), company.trim(), email.trim(), password))
            } catch (e: Exception) {
                AuthState.Error(e.message ?: "Sign-up failed. Please try again.")
            }
        }
    }

    fun resetState() { _state.value = AuthState.Idle }
}