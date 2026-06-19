package com.pay.eeaapp.domain.usecase

import android.content.Context
import android.net.Uri
import com.pay.eeaapp.di.ServiceLocator.getProjectRepository
import com.pay.eeaapp.domain.ProjectRepository
import com.pay.eeaapp.domain.auth.AuthRepository
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDetail
import com.pay.eeaapp.domain.models.ProjectStats
import com.pay.eeaapp.domain.models.ProjectStatus
import kotlinx.coroutines.flow.Flow
class SubmitProjectApplicationUseCase(
    private val projectRepo: ProjectRepository,
    private val session: GetSessionUseCase
) {
    suspend operator fun invoke(
        proponentName: String,
        companyName: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        documentUris: List<Uri>,
        fileNames: List<String>
    ): Result<Project> {
        val uid = session.currentUid
            ?: return Result.failure(IllegalStateException("LOGIN_REQUIRED"))

        val project = projectRepo.submitNewProject(
            proponentUid  = uid,
            proponentName = proponentName,
            companyName   = companyName,
            title         = title,
            description   = description,
            latitude      = latitude,
            longitude     = longitude,
            documentUris  = documentUris,
            fileNames     = fileNames
        )
        return Result.success(project)
    }
}

class ResubmitProjectUseCase(
    private val projectRepo: ProjectRepository,
    private val session: GetSessionUseCase
) {
    suspend operator fun invoke(
        projectId: String,
        documentUris: List<Uri>,
        fileNames: List<String>
    ): Result<Unit> {
        val uid = session.currentUid
            ?: return Result.failure(IllegalStateException("LOGIN_REQUIRED"))
        projectRepo.resubmitProject(projectId, uid, documentUris, fileNames)
        return Result.success(Unit)
    }
}

class ObserveMyProjectsUseCase(private val projectRepo: ProjectRepository) {
    operator fun invoke(uid: String): Flow<List<Project>> =
        projectRepo.observeProjectsForProponent(uid)
}

class ObserveProjectDetailUseCase(private val projectRepo: ProjectRepository) {
    operator fun invoke(projectId: String): Flow<Project?> =
        projectRepo.observeProjectById(projectId)

    suspend fun syncDetails(projectId: String) =
        projectRepo.syncProjectDetails(projectId)
}








