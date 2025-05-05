package com.example.spliteasy.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Extension function to get an ID token from a FirebaseUser using coroutines
 */
suspend fun FirebaseUser.getIdToken(forceRefresh: Boolean) = this.getIdToken(forceRefresh).await()