package com.example.spliteasy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spliteasy.auth.AuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Main ViewModel that handles user authentication state and UI state
 * for the app's navigation flow.
 */
class MainViewModel : ViewModel() {

    // UI state for the main activity
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Initialize with current user
        _uiState.update { it.copy(currentUser = AuthManager.getCurrentUser()) }
    }

    /**
     * Update the current user
     */
    fun updateUser(user: FirebaseUser?) {
        _uiState.update { it.copy(currentUser = user) }
    }

    /**
     * Show the logout confirmation dialog
     */
    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    /**
     * Hide the logout confirmation dialog
     */
    fun hideLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    /**
     * Sign the user out
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                AuthManager.signOut()
                _uiState.update {
                    it.copy(
                        currentUser = null,
                        showLogoutDialog = false
                    )
                }
            } catch (e: Exception) {
                // Handle sign out errors
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}

/**
 * Data class representing the UI state for the main activity
 */
data class MainUiState(
    val currentUser: FirebaseUser? = null,
    val showLogoutDialog: Boolean = false,
    val errorMessage: String? = null
)