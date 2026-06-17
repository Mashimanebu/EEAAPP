package com.pay.eeaapp.domain.usecase

import com.pay.eeaapp.domain.ProjectRepository

class SyncAllProjectsUseCase(private val projectRepo: ProjectRepository) {
    suspend operator fun invoke() = projectRepo.syncAllProjects()
}

class SyncMyProjectsUseCase(private val projectRepo: ProjectRepository) {
    suspend operator fun invoke(uid: String) = projectRepo.syncProjectsForProponent(uid)
}