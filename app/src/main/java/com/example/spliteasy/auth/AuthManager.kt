package com.example.spliteasy.auth

import android.content.Context
import android.util.Log
import androidx.credentials.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

object AuthManager {
    private const val TAG = "AuthManager"
    private lateinit var credentialManager: CredentialManager
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun init(context: Context) {
        credentialManager = CredentialManager.create(context)
    }

    suspend fun signIn(context: Context): FirebaseUser? {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(com.example.spliteasy.R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
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
                firebaseAuth.currentUser
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "signIn failed: ${e.localizedMessage}")
            null
        }
    }

    suspend fun signOut(context: Context) {
        firebaseAuth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing credentials: ${e.localizedMessage}")
        }
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
