package com.example.spliteasy.auth

import android.content.Context
import android.util.Log
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthState(
    val user: FirebaseUser?=null,
    val loading: Boolean=false,
    val error: Exception?=null,
)

object AuthManager {


    private const val TAG = "AuthManager"
    private lateinit var credentialManager: CredentialManager
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun init(context: Context) {
        credentialManager = CredentialManager.create(context)
        if(firebaseAuth.currentUser != null) {
            _state.value =_state.value.copy(user = firebaseAuth.currentUser)
        }
    }

    suspend fun signIn(context: Context): FirebaseUser? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(com.example.spliteasy.R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        _state.value = _state.value.copy(loading = true)
        try {
            val result = credentialManager.getCredential(context, request)
            /**
             * Checks if the credential received from the Credential Manager is of type CustomCredential
             * and specifically a Google ID Token. If so, it extracts the idToken from it.
             */
            val idToken = when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } else null
                }
                else -> null
            }

            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(firebaseCredential).await()
                _state.value =_state.value.copy(user = firebaseAuth.currentUser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "signIn failed: ${e.localizedMessage}")
            _state.value =_state.value.copy(error = e)
        }
        _state.value = _state.value.copy(loading = false)
        return _state.value.user
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        _state.value = _state.value.copy(user = null, loading = false, error = null)
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials: ${e.localizedMessage}")
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
