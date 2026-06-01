package com.zando.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.zando.app.model.FirestoreService
import com.zando.app.model.UserProfile
import com.zando.app.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class AuthViewModel(
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                val profile = firestoreService.getUserProfile(currentUser.uid)
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Login failed")
                var profile = firestoreService.getUserProfile(uid)
                
                // Hardcoded Admin logic
                if (email == "admin@gmail.com" && pass == "admin1234") {
                    if (profile == null || profile.role != UserRole.ADMIN) {
                        profile = UserProfile(uid, "Admin", email, UserRole.ADMIN)
                        firestoreService.saveUserProfile(profile)
                    }
                }

                if (profile != null) {
                    if (profile.isBlocked) {
                        auth.signOut()
                        throw Exception("Your account is blocked.")
                    }
                    _uiState.update { it.copy(userProfile = profile, loginSuccess = true) }
                } else {
                    val newProfile = UserProfile(uid, "User", email, UserRole.USER)
                    firestoreService.saveUserProfile(newProfile)
                    _uiState.update { it.copy(userProfile = newProfile, loginSuccess = true) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: throw Exception("Registration failed")
                val profile = UserProfile(uid, name, email, UserRole.USER)
                firestoreService.saveUserProfile(profile)
                _uiState.update { it.copy(userProfile = profile, loginSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.update { it.copy(userProfile = null, loginSuccess = false) }
    }
    
    fun resetSuccess() {
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
