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
            val uid = getSession.currentUser()?.uid ?: return@launch

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


class ApplyProjectViewModel(app: Application) : AndroidViewModel(app) {

    private val submitUseCase = ServiceLocator.submitProjectApplicationUseCase(app)
    private val getSession    = ServiceLocator.getSessionUseCase(app)

    private val _uiState = MutableStateFlow(ApplyUiState())
    val uiState: StateFlow<ApplyUiState> = _uiState.asStateFlow()

    fun onTitleChange(v: String)       { _uiState.update { it.copy(title = v) } }
    fun onDescriptionChange(v: String) { _uiState.update { it.copy(description = v) } }
    fun onCompanyNameChange(v: String) { _uiState.update { it.copy(companyName = v) } }
    fun onLatitudeChange(v: String)    { _uiState.update { it.copy(latitude = v) } }
    fun onLongitudeChange(v: String)   { _uiState.update { it.copy(longitude = v) } }

    fun addFiles(files: List<Pair<Uri, String>>) {
        _uiState.update { it.copy(pickedFiles = it.pickedFiles + files) }
    }
    fun removeFile(index: Int) {
        _uiState.update { it.copy(pickedFiles = it.pickedFiles.toMutableList().also { l -> l.removeAt(index) }) }
    }

    fun setLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat.toString(), longitude = lng.toString()) }
    }

    fun submit() {
        val s = _uiState.value
        val lat = s.latitude.toDoubleOrNull()
        val lng = s.longitude.toDoubleOrNull()
        when {
            s.title.isBlank()       -> { _uiState.update { it.copy(submitState = SubmitState.Error("Project title is required.")) }; return }
            s.description.isBlank() -> { _uiState.update { it.copy(submitState = SubmitState.Error("Description is required.")) }; return }
            lat == null || lng == null -> { _uiState.update { it.copy(submitState = SubmitState.Error("Enter valid latitude and longitude.")) }; return }
            s.pickedFiles.isEmpty() -> { _uiState.update { it.copy(submitState = SubmitState.Error("Attach at least one document.")) }; return }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(submitState = SubmitState.Loading) }
            val proponent = getSession.currentUser()
            _uiState.update {
                try {
                    submitUseCase(
                        proponentName = proponent?.fullName ?: "",
                        title         = s.title.trim(),
                        description   = s.description.trim(),
                        companyName   = s.companyName.trim(),
                        latitude      = lat,
                        longitude     = lng,
                        documentUris  = s.pickedFiles.map { p -> p.first },
                        fileNames     = s.pickedFiles.map { p -> p.second }
                    )
                    it.copy(submitState = SubmitState.Success)
                } catch (e: Exception) {
                    it.copy(submitState = SubmitState.Error(e.message ?: "Submission failed."))
                }
            }
        }
    }

    fun resetSubmitState() { _uiState.update { it.copy(submitState = SubmitState.Idle) } }
}


class ProjectDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val observeDetail  = ServiceLocator.observeProjectDetailUseCase(app)
    private val resubmit       = ServiceLocator.resubmitProjectUseCase(app)
    private val getSession     = ServiceLocator.getSessionUseCase(app)

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    fun load(projectId: String) {
        viewModelScope.launch {
            observeDetail(projectId).collect { detail ->
                _uiState.update {
                    it.copy(
                        project   = detail,
                        documents = detail?.documents ?: emptyList(),
                        reviews   = detail?.reviews   ?: emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addResubmitFiles(files: List<Pair<Uri, String>>) {
        _uiState.update { it.copy(pickedResubmitFiles = it.pickedResubmitFiles + files) }
    }
    fun removeResubmitFile(index: Int) {
        _uiState.update { it.copy(pickedResubmitFiles = it.pickedResubmitFiles.toMutableList().also { l -> l.removeAt(index) }) }
    }

    fun resubmit(projectId: String) {
        val files = _uiState.value.pickedResubmitFiles
        if (files.isEmpty()) {
            _uiState.update { it.copy(resubmitState = SubmitState.Error("Attach updated documents before resubmitting.")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(resubmitState = SubmitState.Loading) }
            _uiState.update {
                try {
                    resubmit(projectId, files.map { p -> p.first }, files.map { p -> p.second })
                    it.copy(resubmitState = SubmitState.Success, pickedResubmitFiles = emptyList())
                } catch (e: Exception) {
                    it.copy(resubmitState = SubmitState.Error(e.message ?: "Resubmission failed."))
                }
            }
        }
    }

    fun resetResubmitState() { _uiState.update { it.copy(resubmitState = SubmitState.Idle) } }
}