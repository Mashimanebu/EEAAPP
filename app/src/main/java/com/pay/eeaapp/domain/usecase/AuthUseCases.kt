package com.pay.eeaapp.domain.usecase

import com.pay.eeaapp.domain.auth.AuthRepository
import com.pay.eeaapp.domain.models.User
import kotlinx.coroutines.flow.Flow

class GetSessionUseCase(private val repo: AuthRepository) {
    val isLoggedIn: Boolean get() = repo.isLoggedIn
    val currentUid: String? get() = repo.currentUid
    suspend fun currentUser(): User? = currentUid?.let { repo.getCurrentUserOnce(it) }
}

class SignUpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        fullName: String,
        company: String,
        email: String,
        password: String
    ): User = authRepository.signUp(fullName, company, email, password)
}

class SignInUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User =
        authRepository.signIn(email, password)
}

class SignOutUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.signOut()
}

class ObserveCurrentUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(uid: String): Flow<User?> =
        authRepository.observeCurrentUser(uid)
}
