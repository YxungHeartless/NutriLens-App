package com.example.data.repository

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: Flow<AuthUser?> = _currentUser

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        val user = AuthUser("mock-uid", email)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> {
        val user = AuthUser("mock-uid", email)
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        val user = AuthUser("mock-uid", "mockgoogle@example.com")
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        _currentUser.value = null
        return Result.success(Unit)
    }
}
