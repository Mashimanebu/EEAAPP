package com.pay.eeaapp.ui.proponent

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pay.eeaapp.di.ServiceLocator
import com.pay.eeaapp.ui.proponent.state.ApplyUiState
import com.pay.eeaapp.ui.proponent.state.ProjectDetailUiState
import com.pay.eeaapp.ui.proponent.state.ProponentDashboardUiState
import com.pay.eeaapp.ui.proponent.state.SubmitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProponentDashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val observeUser     = ServiceLocator.observeCurrentUserUseCase(app)
    private val observeProjects = ServiceLocator.observeMyProjectsUseCase(app)
    private val syncProjects    = ServiceLocator.syncMyProjectsUseCase(app)
    private val signOut         = ServiceLocator.signOutUseCase(app)
    private val getSession      = ServiceLocator.getSessionUseCase(app)

    private val _uiState = MutableStateFlow(ProponentDashboardUiState())
    val uiState: StateFlow<ProponentDashboardUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = getSession.currentUser()?.uid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            launch {
                observeUser(uid).collect { user ->
                    _uiState.update { it.copy(user = user) }
                }
            }

            try {
                syncProjects(uid)
            } catch (_: Exception) { /* best-effort */ }

            observeProjects(uid).collect { projects ->
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            }
        }
    }

    fun signOut() { signOut.invoke() }
}
