package com.pay.eeaapp.di

import android.content.Context
import com.pay.eeaapp.data.database.AppDatabase
import com.pay.eeaapp.data.remote.FirebaseAuthSource
import com.pay.eeaapp.data.remote.FirebaseStorageSource
import com.pay.eeaapp.data.remote.FirestoreSource
import com.pay.eeaapp.domain.ProjectRepository
import com.pay.eeaapp.domain.auth.AuthRepository

object ServiceLocator {

    @Volatile private var database: AppDatabase? = null

    private val authSource by lazy { FirebaseAuthSource() }
    private val firestoreSource by lazy { FirestoreSource() }
    private val storageSource by lazy { FirebaseStorageSource() }

    fun getDatabase(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }

    fun getAuthRepository(context: Context): AuthRepository {
        val db = getDatabase(context)
        return AuthRepository(authSource, firestoreSource, db.userDao())
    }

    fun getProjectRepository(context: Context): ProjectRepository {
        val db = getDatabase(context)
        return ProjectRepository(
            projectDao = db.projectDao(),
            documentDao = db.documentDao(),
            reviewDao = db.reviewDao(),
            firestoreSource = firestoreSource,
            storageSource = storageSource
        )
    }

    fun getSessionUseCase(context: Context) = GetSessionUseCase(getAuthRepository(context))

    fun signUpUseCase(context: Context) = SignUpUseCase(getAuthRepository(context))
    fun signInUseCase(context: Context) = SignInUseCase(getAuthRepository(context))
    fun signOutUseCase(context: Context) = SignOutUseCase(getAuthRepository(context))
    fun observeCurrentUserUseCase(context: Context) =
        ObserveCurrentUserUseCase(getAuthRepository(context))

    fun submitProjectApplicationUseCase(context: Context) = SubmitProjectApplicationUseCase(
        getProjectRepository(context), getSessionUseCase(context)
    )

    fun resubmitProjectUseCase(context: Context) = ResubmitProjectUseCase(
        getProjectRepository(context), getSessionUseCase(context)
    )

    fun observeMyProjectsUseCase(context: Context) =
        ObserveMyProjectsUseCase(getProjectRepository(context))

    fun observeProjectDetailUseCase(context: Context) =
        ObserveProjectDetailUseCase(getProjectRepository(context))

    fun observeAllProjectsUseCase(context: Context) =
        ObserveAllProjectsUseCase(getProjectRepository(context))

    fun startReviewUseCase(context: Context) =
        StartReviewUseCase(getProjectRepository(context))

    fun requestAmendmentsUseCase(context: Context) = RequestAmendmentsUseCase(
        getProjectRepository(context), getSessionUseCase(context)
    )

    fun approveProjectUseCase(context: Context) = ApproveProjectUseCase(
        getProjectRepository(context), getSessionUseCase(context)
    )

    fun rejectProjectUseCase(context: Context) = RejectProjectUseCase(
        getProjectRepository(context), getSessionUseCase(context)
    )

    fun getProjectStatsUseCase(context: Context) =
        GetProjectStatsUseCase(getProjectRepository(context))

    fun syncAllProjectsUseCase(context: Context) =
        SyncAllProjectsUseCase(getProjectRepository(context))

    fun syncMyProjectsUseCase(context: Context) =
        SyncMyProjectsUseCase(getProjectRepository(context))
}

