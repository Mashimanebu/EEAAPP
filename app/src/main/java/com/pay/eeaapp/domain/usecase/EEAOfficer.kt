package com.pay.eeaapp.domain.usecase

import android.net.Uri
import com.pay.eeaapp.domain.ProjectRepository
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectStats
import com.pay.eeaapp.domain.models.ProjectStatus
import kotlinx.coroutines.flow.Flow

class ObserveAllProjectsUseCase(private val projectRepo: ProjectRepository) {
    operator fun invoke(): Flow<List<Project>> = projectRepo.observeAllProjects()
    operator fun invoke(status: ProjectStatus): Flow<List<Project>> =
        projectRepo.observeProjectsByStatus(status)
}



class StartReviewUseCase(private val projectRepo: ProjectRepository) {
    suspend operator fun invoke(projectId: String) = projectRepo.startReview(projectId)
}


class RequestAmendmentsUseCase(
    private val projectRepo: ProjectRepository,
    private val session: GetSessionUseCase
) {
    suspend operator fun invoke(
        projectId: String,
        comment: String,
        attachmentUris: List<Uri>,
        attachmentNames: List<String>
    ): Result<Unit> {
        val uid = session.currentUid
            ?: return Result.failure(IllegalStateException("LOGIN_REQUIRED"))
        projectRepo.requestAmendments(projectId, uid, comment, attachmentUris, attachmentNames)
        return Result.success(Unit)
    }
}

class ApproveProjectUseCase(
    private val projectRepo: ProjectRepository,
    private val session: GetSessionUseCase
) {
    suspend operator fun invoke(projectId: String, comment: String?): Result<Unit> {
        val uid = session.currentUid
            ?: return Result.failure(IllegalStateException("LOGIN_REQUIRED"))
        projectRepo.approveProject(projectId, uid, comment)
        return Result.success(Unit)
    }
}

class RejectProjectUseCase(
    private val projectRepo: ProjectRepository,
    private val session: GetSessionUseCase
) {
    suspend operator fun invoke(projectId: String, comment: String?): Result<Unit> {
        val uid = session.currentUid
            ?: return Result.failure(IllegalStateException("LOGIN_REQUIRED"))
        projectRepo.rejectProject(projectId, uid, comment)
        return Result.success(Unit)
    }
}

class GetProjectStatsUseCase(private val projectRepo: ProjectRepository) {
    suspend operator fun invoke(): ProjectStats = projectRepo.getProjectStats()
}



