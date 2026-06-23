package com.pay.eeaapp.data.remote

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.util.UUID
class FirebaseStorageSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun uploadProjectDocument(
        projectId: String,
        fileUri: Uri,
        originalFileName: String
    ): String {
        val uid = auth.currentUser?.uid
            ?: throw Exception("User not authenticated. Please log in and try again.")

        return try {
            val safeName = "${UUID.randomUUID()}_$originalFileName"

            val ref = storage.reference.child("users/$uid/documents/$safeName")
            ref.putFile(fileUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: StorageException) {
            throw Exception(
                when (e.errorCode) {
                    StorageException.ERROR_OBJECT_NOT_FOUND -> "Storage location not found. Check Firebase Storage rules."
                    StorageException.ERROR_NOT_AUTHORIZED   -> "Not authorized to upload. Check Firebase Storage rules."
                    StorageException.ERROR_CANCELED         -> "Upload was cancelled."
                    else                                    -> "File upload failed: ${e.message}"
                }
            )
        }
    }
}