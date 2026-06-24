package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val isLoading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    
    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess: SharedFlow<Boolean> = _loginSuccess

    fun onEmailChanged(value: String) {
        email.value = value
    }

    fun onPasswordChanged(value: String) {
        password.value = value
    }

    fun signIn() {
        val emailVal = email.value.trim()
        val passwordVal = password.value
        if (emailVal.isEmpty() || passwordVal.isEmpty()) {
            error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            authRepository.signInWithEmail(emailVal, passwordVal)
                .onSuccess {
                    _loginSuccess.emit(true)
                }
                .onFailure { t ->
                    error.value = t.message ?: "Sign in failed"
                }
            isLoading.value = false
        }
    }

    fun signUp() {
        val emailVal = email.value.trim()
        val passwordVal = password.value
        if (emailVal.isEmpty() || passwordVal.isEmpty()) {
            error.value = "Email and password cannot be empty"
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            authRepository.signUpWithEmail(emailVal, passwordVal)
                .onSuccess {
                    _loginSuccess.emit(true)
                }
                .onFailure { t ->
                    error.value = t.message ?: "Sign up failed"
                }
            isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null
            authRepository.signInWithGoogle(idToken)
                .onSuccess {
                    _loginSuccess.emit(true)
                }
                .onFailure { t ->
                    error.value = t.message ?: "Google Sign-In failed"
                }
            isLoading.value = false
        }
    }

    fun clearError() {
        error.value = null
    }
}
