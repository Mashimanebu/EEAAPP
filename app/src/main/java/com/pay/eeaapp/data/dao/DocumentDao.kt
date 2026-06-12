package com.pay.eeaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pay.eeaapp.data.entities.ProjectDocumentEntity
import com.pay.eeaapp.data.entities.ReviewCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: ProjectDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<ProjectDocumentEntity>)

    @Query("SELECT * FROM project_documents WHERE projectId = :projectId ORDER BY uploadedAt ASC")
    fun observeDocumentsForProject(projectId: String): Flow<List<ProjectDocumentEntity>>
}

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewCommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ReviewCommentEntity>)

    @Query("SELECT * FROM review_comments WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun observeReviewsForProject(projectId: String): Flow<List<ReviewCommentEntity>>
}