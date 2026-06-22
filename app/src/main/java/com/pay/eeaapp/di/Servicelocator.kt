package com.pay.eeaapp.di

import android.content.Context
import com.pay.eeaapp.data.database.AppDatabase
import com.pay.eeaapp.data.remote.FirebaseAuthSource
import com.pay.eeaapp.data.remote.FirebaseStorageSource
import com.pay.eeaapp.data.remote.FirestoreSource
import com.pay.eeaapp.domain.ProjectRepository
import com.pay.eeaapp.domain.auth.AuthRepository
import com.pay.eeaapp.domain.usecase.ApproveProjectUseCase
import com.pay.eeaapp.domain.usecase.GetProjectStatsUseCase
import com.pay.eeaapp.domain.usecase.GetSessionUseCase
import com.pay.eeaapp.domain.usecase.ObserveAllProjectsUseCase
import com.pay.eeaapp.domain.usecase.ObserveCurrentUserUseCase
import com.pay.eeaapp.domain.usecase.ObserveMyProjectsUseCase
import com.pay.eeaapp.domain.usecase.ObserveProjectDetailUseCase
import com.pay.eeaapp.domain.usecase.RejectProjectUseCase
import com.pay.eeaapp.domain.usecase.RequestAmendmentsUseCase
import com.pay.eeaapp.domain.usecase.ResubmitProjectUseCase
import com.pay.eeaapp.domain.usecase.SignInUseCase
import com.pay.eeaapp.domain.usecase.SignOutUseCase
import com.pay.eeaapp.domain.usecase.SignUpUseCase
import com.pay.eeaapp.domain.usecase.StartReviewUseCase
import com.pay.eeaapp.domain.usecase.SubmitProjectApplicationUseCase
import com.pay.eeaapp.domain.usecase.SyncAllProjectsUseCase
import com.pay.eeaapp.domain.usecase.SyncMyProjectsUseCase


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

    fun getUserDao(context: Context) = getDatabase(context).userDao()
}

