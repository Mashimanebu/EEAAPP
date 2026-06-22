package com.pay.eeaapp.ui.proponent.state

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pay.eeaapp.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus


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