package com.pay.eeaapp.domain

import android.icu.text.SimpleDateFormat
import android.net.Uri
import com.pay.eeaapp.data.dao.DocumentDao
import com.pay.eeaapp.data.dao.ProjectDao
import com.pay.eeaapp.data.dao.ReviewDao
import com.pay.eeaapp.data.entities.ProjectDocumentEntity
import com.pay.eeaapp.data.entities.ProjectEntity
import com.pay.eeaapp.data.entities.ReviewCommentEntity
import com.pay.eeaapp.data.mappers.toDomain
import com.pay.eeaapp.data.remote.FirebaseStorageSource
import com.pay.eeaapp.data.remote.FirestoreSource
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDetail
import com.pay.eeaapp.domain.models.ProjectStats
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.sql.Date
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.collections.forEachIndexed

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val documentDao: DocumentDao,
    private val reviewDao: ReviewDao,
    private val firestoreSource: FirestoreSource,
    private val storageSource: FirebaseStorageSource
) {

    suspend fun syncAllProjects() {
        firestoreSource.observeAllProjects().collectLatestInto { remoteProjects ->
            projectDao.upsertAll(remoteProjects)
        }
    }

    suspend fun syncProjectsForProponent(uid: String) {
        firestoreSource.observeProjectsForProponent(uid).collectLatestInto { remoteProjects ->
            projectDao.upsertAll(remoteProjects)
        }
    }

    suspend fun syncProjectDetails(projectId: String) {
        firestoreSource.observeDocuments(projectId).collectLatestInto { docs ->
            documentDao.insertAll(docs)
        }
        firestoreSource.observeReviews(projectId).collectLatestInto { reviews ->
            reviewDao.insertAll(reviews)
        }
    }


    fun observeProjectsForProponent(uid: String): Flow<List<Project>> =
        projectDao.observeProjectsForProponent(uid).map { list -> list.map { it.toDomain() } }

    fun observeAllProjects(): Flow<List<Project>> =
        projectDao.observeAllProjects().map { list -> list.map { it.toDomain() } }

    fun observeProjectsByStatus(status: ProjectStatus): Flow<List<Project>> =
        projectDao.observeProjectsByStatus(status.name).map { list -> list.map { it.toDomain() } }

    fun observeProjectById(projectId: String): Flow<Project?> =
        projectDao.observeProjectById(projectId).map { it?.toDomain() }



    suspend fun submitNewProject(
        proponentUid: String,
        proponentName: String,
        companyName: String,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        documentUris: List<Uri>,
        fileNames: List<String>
    ): Project {
        val now = System.currentTimeMillis()
        val projectId = UUID.randomUUID().toString()

        val entity = ProjectEntity(
            id = projectId,
            proponentUid = proponentUid,
            proponentName = proponentName,
            companyName = companyName,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            status = ProjectStatus.SUBMITTED.name,
            createdAt = now,
            updatedAt = now
        )

        projectDao.upsert(entity)
        firestoreSource.upsertProject(entity)

        uploadAndAttachDocuments(
            projectId = projectId,
            uploaderUid = proponentUid,
            uploaderRole = UserRole.PROPONENT,
            documentUris = documentUris,
            fileNames = fileNames
        )

        return entity.toDomainWithEmptyChildren()
    }



    suspend fun resubmitProject(
        projectId: String,
        proponentUid: String,
        documentUris: List<Uri>,
        fileNames: List<String>
    ) {
        uploadAndAttachDocuments(
            projectId = projectId,
            uploaderUid = proponentUid,
            uploaderRole = UserRole.PROPONENT,
            documentUris = documentUris,
            fileNames = fileNames
        )
        updateStatus(projectId, ProjectStatus.SUBMITTED)
    }

    suspend fun startReview(projectId: String) {
        updateStatus(projectId, ProjectStatus.UNDER_REVIEW)
    }

    suspend fun requestAmendments(
        projectId: String,
        officerUid: String,
        comment: String,
        attachmentUris: List<Uri>,
        attachmentNames: List<String>
    ) {
        addReview(projectId, officerUid, comment)
        uploadAndAttachDocuments(
            projectId = projectId,
            uploaderUid = officerUid,
            uploaderRole = UserRole.ADMIN,
            documentUris = attachmentUris,
            fileNames = attachmentNames
        )
        updateStatus(projectId, ProjectStatus.AMENDMENTS_REQUIRED)
    }
    suspend fun addReviewComment(projectId: String, officerUid: String, comment: String) {
        addReview(projectId, officerUid, comment)
    }

    suspend fun approveProject(projectId: String, officerUid: String, comment: String?) {
        if (!comment.isNullOrBlank()) addReview(projectId, officerUid, comment)
        updateStatus(projectId, ProjectStatus.APPROVED)
    }

    suspend fun rejectProject(projectId: String, officerUid: String, comment: String?) {
        if (!comment.isNullOrBlank()) addReview(projectId, officerUid, comment)
        updateStatus(projectId, ProjectStatus.REJECTED)
    }

    suspend fun getProjectStats(): ProjectStats {
        val statusCounts = projectDao.getStatusCounts().associate { it.status to it.count }
        val timestamps = projectDao.getAllCreatedAtTimestamps()

        return ProjectStats(
            totalSubmitted = statusCounts.values.sum(),
            totalApproved = statusCounts[ProjectStatus.APPROVED.name] ?: 0,
            totalRejected = statusCounts[ProjectStatus.REJECTED.name] ?: 0,
            totalUnderReview = statusCounts[ProjectStatus.UNDER_REVIEW.name] ?: 0,
            weekly = bucketByDayOfWeek(timestamps),
            monthly = bucketByMonth(timestamps),
            yearly = bucketByYear(timestamps)
        )
    }

    private suspend fun uploadAndAttachDocuments(
        projectId: String,
        uploaderUid: String,
        uploaderRole: UserRole,
        documentUris: List<Uri>,
        fileNames: List<String>
    ) {
        documentUris.forEachIndexed { index, uri ->
            val name = fileNames.getOrElse(index) { "document_$index.pdf" }
            val url = storageSource.uploadProjectDocument(projectId, uri, name)
            val doc = ProjectDocumentEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                fileName = name,
                fileUrl = url,
                uploadedByUid = uploaderUid,
                uploadedByRole = uploaderRole.name,
                uploadedAt = System.currentTimeMillis()
            )
            documentDao.insert(doc)
            firestoreSource.addDocument(doc)
        }
    }

    private suspend fun addReview(projectId: String, officerUid: String, comment: String) {
        val review = ReviewCommentEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            officerUid = officerUid,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )
        reviewDao.insert(review)
        firestoreSource.addReview(review)
    }

    private suspend fun updateStatus(projectId: String, status: ProjectStatus) {
        val now = System.currentTimeMillis()
        projectDao.updateStatus(projectId, status.name, now)
        firestoreSource.updateProjectStatus(projectId, status.name, now)
    }

    private fun ProjectEntity.toDomainWithEmptyChildren(): Project = Project(
        id = id,
        proponentUid = proponentUid,
        proponentName = proponentName,
        companyName = companyName,
        title = title,
        description = description,
        latitude = latitude,
        longitude = longitude,
        status = ProjectStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun bucketByDayOfWeek(timestamps: List<Long>): Map<String, Int> {
        val labels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val result = linkedMapOf<String, Int>().apply { labels.forEach { put(it, 0) } }
        val cal = Calendar.getInstance()
        val weekAgo = cal.timeInMillis - 7L * 24 * 60 * 60 * 1000
        timestamps.filter { it >= weekAgo }.forEach { ts ->
            cal.timeInMillis = ts
            val label = labels[cal.get(Calendar.DAY_OF_WEEK) - 1]
            result[label] = (result[label] ?: 0) + 1
        }
        return result
    }

    private fun bucketByMonth(timestamps: List<Long>): Map<String, Int> {
        val format = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val result = linkedMapOf<String, Int>()
        val cal = Calendar.getInstance()

        for (i in 11 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            result[format.format(c.time)] = 0
        }
        timestamps.forEach { ts ->
            val label = format.format(Date(ts))
            if (result.containsKey(label)) {
                result[label] = (result[label] ?: 0) + 1
            }
        }
        return result
    }

    private fun bucketByYear(timestamps: List<Long>): Map<String, Int> {
        val format = SimpleDateFormat("yyyy", Locale.getDefault())
        val result = linkedMapOf<String, Int>()
        timestamps.forEach { ts ->
            val label = format.format(Date(ts))
            result[label] = (result[label] ?: 0) + 1
        }
        return result.toSortedMap()
    }

    private suspend fun <T> Flow<T>.collectLatestInto(action: suspend (T) -> Unit) {
        this.collect { action(it) }
    }
}