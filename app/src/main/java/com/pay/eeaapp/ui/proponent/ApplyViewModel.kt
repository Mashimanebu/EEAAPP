package com.pay.eeaapp.ui.proponent

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pay.eeaapp.di.ServiceLocator
import com.pay.eeaapp.ui.proponent.state.ApplyUiState
import com.pay.eeaapp.ui.proponent.state.SubmitState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

class ApplyProjectViewModel(app: Application) : AndroidViewModel(app) {

    private val submitUseCase = ServiceLocator.submitProjectApplicationUseCase(app)
    private val session       = ServiceLocator.getSessionUseCase(app)   // ← renamed for clarity

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
        val s   = _uiState.value
        val lat = s.latitude.toDoubleOrNull()
        val lng = s.longitude.toDoubleOrNull()

        when {
            s.title.isBlank()          -> { _uiState.update { it.copy(submitState = SubmitState.Error("Project title is required.")) };       return }
            s.description.isBlank()    -> { _uiState.update { it.copy(submitState = SubmitState.Error("Description is required.")) };         return }
            s.companyName.isBlank()    -> { _uiState.update { it.copy(submitState = SubmitState.Error("Company name is required.")) };        return }
            lat == null || lng == null -> { _uiState.update { it.copy(submitState = SubmitState.Error("Enter valid latitude and longitude.")) }; return }
            s.pickedFiles.isEmpty()    -> { _uiState.update { it.copy(submitState = SubmitState.Error("Attach at least one document.")) };    return }
        }

        val uid = session.currentUid
        if (uid == null) {
            _uiState.update { it.copy(submitState = SubmitState.Error("Session expired. Please log in again.")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(submitState = SubmitState.Loading) }

            val proponentName = session.currentUser()?.fullName ?: ""

            val result = submitUseCase(
                proponentName = proponentName,
                companyName   = s.companyName.trim(),
                title         = s.title.trim(),
                description   = s.description.trim(),
                latitude      = lat!!,
                longitude     = lng!!,
                documentUris  = s.pickedFiles.map { it.first },
                fileNames     = s.pickedFiles.map { it.second }
            )

            val newState = result.fold(
                onSuccess = { SubmitState.Success },
                onFailure = { e ->
                    val msg = if (e.message == "LOGIN_REQUIRED")
                        "Session expired. Please log in again."
                    else
                        e.message ?: "Submission failed."
                    SubmitState.Error(msg)
                }
            )
            _uiState.update { it.copy(submitState = newState) }
        }
    }

    fun resetSubmitState() { _uiState.update { it.copy(submitState = SubmitState.Idle) } }
}

