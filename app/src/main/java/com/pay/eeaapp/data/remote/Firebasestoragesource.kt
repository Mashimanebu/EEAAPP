package com.pay.eeaapp.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun uploadProjectDocument(
        projectId: String,
        fileUri: Uri,
        originalFileName: String
    ): String {
        val safeName = "${UUID.randomUUID()}_$originalFileName"
        val ref = storage.reference.child("projects/$projectId/$safeName")
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }
}
