package com.pay.eeaapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pay.eeaapp.data.entities.ProjectEntity
import com.pay.eeaapp.data.entities.ProjectWithDetails
import kotlinx.coroutines.flow.Flow


@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(projects: List<ProjectEntity>)

    @Query("UPDATE projects SET status = :status, updatedAt = :updatedAt WHERE id = :projectId")
    suspend fun updateStatus(projectId: String, status: String, updatedAt: Long)

    @Transaction
    @Query("SELECT * FROM projects WHERE proponentUid = :uid ORDER BY updatedAt DESC")
    fun observeProjectsForProponent(uid: String): Flow<List<ProjectWithDetails>>

    @Transaction
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAllProjects(): Flow<List<ProjectWithDetails>>

    @Transaction
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY updatedAt DESC")
    fun observeProjectsByStatus(status: String): Flow<List<ProjectWithDetails>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun observeProjectById(projectId: String): Flow<ProjectWithDetails?>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectByIdOnce(projectId: String): ProjectWithDetails?

    @Query("SELECT id, status, latitude, longitude, title FROM projects")
    fun observeAllProjectLocations(): Flow<List<ProjectLocationRow>>

    @Query("SELECT status, COUNT(*) as count FROM projects GROUP BY status")
    suspend fun getStatusCounts(): List<StatusCount>

    @Query("SELECT createdAt FROM projects")
    suspend fun getAllCreatedAtTimestamps(): List<Long>
}

data class ProjectLocationRow(
    val id: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val title: String
)

data class StatusCount(
    val status: String,
    val count: Int
)
