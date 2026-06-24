package com.example.data.repository

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let { AuthUser(it.uid, it.email) }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        continuation.resume(Result.success(AuthUser(user.uid, user.email)))
                    } else {
                        continuation.resume(Result.failure(Exception("Firebase user is null")))
                    }
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception("Sign in failed")))
                }
            }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        continuation.resume(Result.success(AuthUser(user.uid, user.email)))
                    } else {
                        continuation.resume(Result.failure(Exception("Firebase user is null")))
                    }
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception("Sign up failed")))
                }
            }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = suspendCancellableCoroutine { continuation ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        continuation.resume(Result.success(AuthUser(user.uid, user.email)))
                    } else {
                        continuation.resume(Result.failure(Exception("Firebase user is null")))
                    }
                } else {
                    continuation.resume(Result.failure(task.exception ?: Exception("Google sign in failed")))
                }
            }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
