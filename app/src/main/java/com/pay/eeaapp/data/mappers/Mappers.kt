package com.pay.eeaapp.data.mappers

import com.pay.eeaapp.data.entities.ProjectDocumentEntity
import com.pay.eeaapp.data.entities.ProjectEntity
import com.pay.eeaapp.data.entities.ProjectWithDetails
import com.pay.eeaapp.data.entities.ReviewCommentEntity
import com.pay.eeaapp.data.entities.UserEntity
import com.pay.eeaapp.domain.models.ProjectStatus
import com.pay.eeaapp.domain.models.User
import com.pay.eeaapp.domain.models.UserRole
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ReviewComment

fun UserEntity.toDomain(): User = User(
    uid = uid,
    fullName = fullName ?: "",
    email = email ?: "",
    company = company ?: "",
    role = if (role == "ADMIN") UserRole.ADMIN else UserRole.PROPONENT
)

fun User.toEntity(): UserEntity = UserEntity(
    uid = uid,
    fullName = fullName,
    email = email,
    company = company,
    role = role.name
)

fun ProjectDocumentEntity.toDomain(): ProjectDocument = ProjectDocument(
    id = id,
    projectId = projectId,
    fileName = fileName,
    fileUrl = fileUrl,
    uploadedByUid = uploadedByUid,
    uploadedByRole = if (uploadedByRole == "ADMIN") UserRole.ADMIN else UserRole.PROPONENT,
    uploadedAt = uploadedAt
)

fun ProjectDocument.toEntity(): ProjectDocumentEntity = ProjectDocumentEntity(
    id = id,
    projectId = projectId,
    fileName = fileName,
    fileUrl = fileUrl,
    uploadedByUid = uploadedByUid,
    uploadedByRole = uploadedByRole.name,
    uploadedAt = uploadedAt
)

fun ReviewCommentEntity.toDomain(): ReviewComment = ReviewComment(
    id = id,
    projectId = projectId,
    officerUid = officerUid,
    comment = comment,
    createdAt = createdAt
)

fun ReviewComment.toEntity(): ReviewCommentEntity = ReviewCommentEntity(
    id = id,
    projectId = projectId,
    officerUid = officerUid,
    comment = comment,
    createdAt = createdAt
)

fun ProjectWithDetails.toDomain(): Project = Project(
    id = project.id,
    proponentUid = project.proponentUid,
    proponentName = project.proponentName,
    companyName = project.companyName,
    title = project.title,
    description = project.description,
    latitude = project.latitude,
    longitude = project.longitude,
    status = ProjectStatus.valueOf(project.status),
    createdAt = project.createdAt,
    updatedAt = project.updatedAt,
    documents = documents.map { it.toDomain() },
    reviews = reviews.map { it.toDomain() }
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    id = id,
    proponentUid = proponentUid,
    proponentName = proponentName,
    companyName = companyName,
    title = title,
    description = description,
    latitude = latitude,
    longitude = longitude,
    status = status.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)