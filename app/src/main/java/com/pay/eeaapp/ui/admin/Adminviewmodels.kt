package com.pay.eeaapp.ui.admin

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pay.eeaapp.di.ServiceLocator
import com.pay.eeaapp.ui.admin.state.AdminDashboardUiState
import com.pay.eeaapp.ui.admin.state.AdminFilter
import com.pay.eeaapp.ui.admin.state.AdminReviewUiState
import com.pay.eeaapp.ui.admin.state.AnalyticsPeriod
import com.pay.eeaapp.ui.admin.state.AnalyticsUiState
import com.pay.eeaapp.ui.admin.state.MapUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AdminDashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val observeUser = ServiceLocator.observeCurrentUserUseCase(app)
    private val observeAll = ServiceLocator.observeAllProjectsUseCase(app)
    private val syncAll = ServiceLocator.syncAllProjectsUseCase(app)
    private val signOut = ServiceLocator.signOutUseCase(app)
    private val getSession = ServiceLocator.getSessionUseCase(app)

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val uid = getSession.currentUser()?.uid ?: return@launch
            launch {
                observeUser(uid).collect { user ->
                    _uiState.update { it.copy(user = user) }
                }
            }

            try {
                syncAll()
            } catch (_: Exception) {
            }
            observeAll().collect { projects ->
                _uiState.update { it.copy(allProjects = projects, isLoading = false) }
            }
        }
    }

    fun setFilter(filter: AdminFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun signOut() {
        signOut.invoke()
    }
}


class AdminProjectReviewViewModel(app: Application) : AndroidViewModel(app) {

    private val observeDetail = ServiceLocator.observeProjectDetailUseCase(app)
    private val startReview = ServiceLocator.startReviewUseCase(app)
    private val requestAmendments = ServiceLocator.requestAmendmentsUseCase(app)
    private val approve = ServiceLocator.approveProjectUseCase(app)
    private val reject = ServiceLocator.rejectProjectUseCase(app)

    private val _uiState = MutableStateFlow(AdminReviewUiState())
    val uiState: StateFlow<AdminReviewUiState> = _uiState.asStateFlow()

    fun load(projectId: String) {
        viewModelScope.launch {
            observeDetail(projectId).collect { detail ->
                _uiState.update {
                    it.copy(

                        project = detail,
                        documents = detail?.documents ?: emptyList(),
                        reviews = detail?.reviews ?: emptyList(),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onCommentChange(v: String) {
        _uiState.update { it.copy(comment = v) }
    }

    fun addAttachments(files: List<Pair<Uri, String>>) {
        _uiState.update { it.copy(attachments = it.attachments + files) }
    }

    fun removeAttachment(index: Int) {
        _uiState.update {
            it.copy(
                attachments = it.attachments.toMutableList().also { l -> l.removeAt(index) })
        }
    }

    fun startReview(projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ReviewAction.Loading) }
            try {
                startReview.invoke(projectId)
                _uiState.update { it.copy(actionState = ReviewAction.Done) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = ReviewAction.Error(
                            e.message ?: "Failed."
                        )
                    )
                }
            }
        }
    }

    fun requestAmendments(projectId: String) {
        val s = _uiState.value
        if (s.comment.isBlank()) {
            _uiState.update { it.copy(actionState = ReviewAction.Error("Add a comment explaining what amendments are needed.")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ReviewAction.Loading) }
            try {
                requestAmendments.invoke(
                    projectId = projectId,
                    comment = s.comment,
                    attachmentUris = s.attachments.map { it.first },
                    attachmentNames = s.attachments.map { it.second }
                )
                _uiState.update {
                    it.copy(
                        actionState = ReviewAction.Done,
                        comment = "",
                        attachments = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = ReviewAction.Error(
                            e.message ?: "Failed."
                        )
                    )
                }
            }
        }
    }

    fun approve(projectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ReviewAction.Loading) }
            try {
                approve.invoke(projectId,_uiState.value.comment.ifBlank { null })
                _uiState.update { it.copy(actionState = ReviewAction.Done) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = ReviewAction.Error(
                            e.message ?: "Failed."
                        )
                    )
                }
            }
        }
    }

    fun reject(projectId: String) {
        val s = _uiState.value
        if (s.comment.isBlank()) {
            _uiState.update { it.copy(actionState = ReviewAction.Error("Provide a reason for rejection.")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionState = ReviewAction.Loading) }
            try {
                reject.invoke(projectId, s.comment)
                _uiState.update { it.copy(actionState = ReviewAction.Done) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actionState = ReviewAction.Error(
                            e.message ?: "Failed."
                        )
                    )
                }
            }
        }
    }

    fun resetActionState() {
        _uiState.update { it.copy(actionState = ReviewAction.Idle) }
    }
}


class AnalyticsViewModel(app: Application) : AndroidViewModel(app) {

    private val getStats = ServiceLocator.getProjectStatsUseCase(app)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val stats = getStats()
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load stats."
                    )
                }
            }
        }
    }

    fun setPeriod(p: AnalyticsPeriod) {
        _uiState.update { it.copy(period = p) }
    }
}

class MapViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.getProjectRepository(app)
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAllProjectLocations().collect { locations ->
                _uiState.update { it.copy(locations = locations, isLoading = false) }
            }
        }
    }
}