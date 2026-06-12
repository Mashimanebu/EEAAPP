package com.pay.eeaapp.domain.usecase

import android.net.Uri
import com.pay.eeaapp.domain.ProjectRepository
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectStats
import kotlinx.coroutines.flow.Flow

class SubmitProjectApplicationUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke(
        companyName: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        documentUris: List<Uri>,
        fileNames: List<String>
    ): Project {
        val user = getSessionUseCase.getCurrentUser()
            ?: throw IllegalStateException("No active session.")
        return projectRepository.submitNewProject(
            proponentUid = user.uid,
            proponentName = user.fullName,
            companyName = companyName,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            documentUris = documentUris,
            fileNames = fileNames
        )
    }
}

class ResubmitProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke(
        projectId: String,
        documentUris: List<Uri>,
        fileNames: List<String>
    ) {
        val uid = getSessionUseCase.currentUid
            ?: throw IllegalStateException("No active session.")
        projectRepository.resubmitProject(projectId, uid, documentUris, fileNames)
    }
}

class ObserveMyProjectsUseCase(private val projectRepository: ProjectRepository) {
    operator fun invoke(uid: String): Flow<List<Project>> =
        projectRepository.observeProjectsForProponent(uid)
}

class ObserveProjectDetailUseCase(private val projectRepository: ProjectRepository) {
    operator fun invoke(projectId: String): Flow<Project?> =
        projectRepository.observeProjectById(projectId)
}

class ObserveAllProjectsUseCase(private val projectRepository: ProjectRepository) {
    operator fun invoke(): Flow<List<Project>> =
        projectRepository.observeAllProjects()
}


class StartReviewUseCase(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke(projectId: String) =
        projectRepository.startReview(projectId)
}

class RequestAmendmentsUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke(
        projectId: String,
        comment: String,
        attachmentUris: List<Uri> = emptyList(),
        attachmentNames: List<String> = emptyList()
    ) {
        val uid = getSessionUseCase.currentUid
            ?: throw IllegalStateException("No active session.")
        projectRepository.requestAmendments(projectId, uid, comment, attachmentUris, attachmentNames)
    }
}

class ApproveProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke(projectId: String, comment: String? = null) {
        val uid = getSessionUseCase.currentUid
            ?: throw IllegalStateException("No active session.")
        projectRepository.approveProject(projectId, uid, comment)
    }
}

class RejectProjectUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke(projectId: String, comment: String? = null) {
        val uid = getSessionUseCase.currentUid
            ?: throw IllegalStateException("No active session.")
        projectRepository.rejectProject(projectId, uid, comment)
    }
}

class GetProjectStatsUseCase(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke(): ProjectStats =
        projectRepository.getProjectStats()
}

class SyncAllProjectsUseCase(private val projectRepository: ProjectRepository) {
    suspend operator fun invoke() = projectRepository.syncAllProjects()
}

class SyncMyProjectsUseCase(
    private val projectRepository: ProjectRepository,
    private val getSessionUseCase: GetSessionUseCase
) {
    suspend operator fun invoke() {
        val uid = getSessionUseCase.currentUid
            ?: throw IllegalStateException("No active session.")
        projectRepository.syncProjectsForProponent(uid)
    }
}
