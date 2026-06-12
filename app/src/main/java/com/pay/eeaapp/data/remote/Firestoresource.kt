package com.pay.eeaapp.data.remote

import androidx.room.Query
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pay.eeaapp.data.entities.ProjectDocumentEntity
import com.pay.eeaapp.data.entities.ProjectEntity
import com.pay.eeaapp.data.entities.ReviewCommentEntity
import com.pay.eeaapp.data.entities.UserEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.collections.emptyList
import kotlin.jvm.java

class FirestoreSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCol get() = db.collection("users")
    private val projectsCol get() = db.collection("projects")


    suspend fun upsertUser(user: UserEntity) {
        usersCol.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): UserEntity? {
        val snap = usersCol.document(uid).get().await()
        return snap.toObject(UserEntity::class.java)
    }

    suspend fun upsertProject(project: ProjectEntity) {
        projectsCol.document(project.id).set(project).await()
    }

    suspend fun updateProjectStatus(projectId: String, status: String, updatedAt: Long) {
        projectsCol.document(projectId)
            .update(mapOf("status" to status, "updatedAt" to updatedAt))
            .await()
    }

    fun observeAllProjects(): Flow<List<ProjectEntity>> = callbackFlow {
        val registration: ListenerRegistration = projectsCol
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.documents?.mapNotNull { it.toObject(ProjectEntity::class.java) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun observeProjectsForProponent(uid: String): Flow<List<ProjectEntity>> = callbackFlow {
        val registration: ListenerRegistration = projectsCol
            .whereEqualTo("proponentUid", uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.documents?.mapNotNull { it.toObject(ProjectEntity::class.java) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addDocument(document: ProjectDocumentEntity) {
        projectsCol.document(document.projectId)
            .collection("documents")
            .document(document.id)
            .set(document)
            .await()
    }

    fun observeDocuments(projectId: String): Flow<List<ProjectDocumentEntity>> = callbackFlow {
        val registration = projectsCol.document(projectId)
            .collection("documents")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.documents?.mapNotNull { it.toObject(ProjectDocumentEntity::class.java) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addReview(review: ReviewCommentEntity) {
        projectsCol.document(review.projectId)
            .collection("reviews")
            .document(review.id)
            .set(review)
            .await()
    }

    fun observeReviews(projectId: String): Flow<List<ReviewCommentEntity>> = callbackFlow {
        val registration = projectsCol.document(projectId)
            .collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val items = snapshot?.documents?.mapNotNull { it.toObject(ReviewCommentEntity::class.java) }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }
}