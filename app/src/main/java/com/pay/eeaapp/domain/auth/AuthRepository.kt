package com.pay.eeaapp.domain.auth

import com.pay.eeaapp.data.dao.UserDao
import com.pay.eeaapp.data.mappers.toDomain
import com.pay.eeaapp.data.mappers.toEntity
import com.pay.eeaapp.data.remote.FirebaseAuthSource
import com.pay.eeaapp.data.remote.FirestoreSource
import com.pay.eeaapp.domain.models.User
import com.pay.eeaapp.domain.models.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val authSource: FirebaseAuthSource,
    private val firestoreSource: FirestoreSource,
    private val userDao: UserDao
) {

    val isLoggedIn: Boolean
        get() = authSource.isLoggedIn()

    val currentUid: String?
        get() = authSource.currentUser?.uid

    suspend fun signUp(
        fullName: String,
        company: String,
        email: String,
        password: String
    ): User {
        val firebaseUser = authSource.signUp(email, password)
        val user = User(
            uid = firebaseUser.uid,
            fullName = fullName,
            email = email,
            company = company,
            role = UserRole.PROPONENT
        )
        firestoreSource.upsertUser(user.toEntity())
        userDao.upsert(user.toEntity())
        return user
    }

    suspend fun signIn(email: String, password: String): User {
        val firebaseUser = authSource.signIn(email, password)
        val remoteUser = firestoreSource.getUser(firebaseUser.uid)
            ?: throw IllegalStateException("No profile found for this account.")
        userDao.upsert(remoteUser)
        return remoteUser.toDomain()
    }

    fun signOut() {
        authSource.signOut()
    }

    fun observeCurrentUser(uid: String): Flow<User?> =
        userDao.observeUser(uid).map { it?.toDomain() }

    suspend fun getCurrentUserOnce(uid: String): User? =
        userDao.getUserOnce(uid)?.toDomain() ?: firestoreSource.getUser(uid)?.toDomain()
}