package week11.st856364.finalproject.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st856364.finalproject.data.repository.AuthRepository
import week11.st856364.finalproject.utils.UiState

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState =
        MutableStateFlow<AuthState>(if (authRepository.currentUser != null)
            AuthState.Authenticated(authRepository.currentUser!!)
        else
            AuthState.Unauthenticated
        )
    val authState: StateFlow<AuthState> = _authState

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _uiState.value = UiState.Loading
            val result = authRepository.signIn(email, password)
            result
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _uiState.value = UiState.Success("Login successful")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Login failed")
                    _uiState.value = UiState.Error(e.message ?: "Login failed")
                }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _uiState.value = UiState.Error("Valid email and 6+ char password required")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _uiState.value = UiState.Loading
            val result = authRepository.signUp(email, password)
            result
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                    _uiState.value = UiState.Success("Registration successful")
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(e.message ?: "Registration failed")
                    _uiState.value = UiState.Error(e.message ?: "Registration failed")
                }
        }
    }

    fun sendResetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = UiState.Error("Email is required")
            return
        }
        _uiState.value = UiState.Loading
        authRepository.sendPasswordReset(email) { result ->
            result
                .onSuccess {
                    _uiState.value =
                        UiState.Success("Password reset link sent to $email")
                }
                .onFailure { e ->
                    _uiState.value =
                        UiState.Error(e.message ?: "Failed to send reset email")
                }
        }
    }

    fun logout() {
        authRepository.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
