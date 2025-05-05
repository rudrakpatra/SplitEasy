package com.example.spliteasy.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(onSignInSuccess: (user:FirebaseUser) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authState by AuthManager.state.collectAsState()

    LaunchedEffect(authState.user) {
        authState.user?.let { onSignInSuccess(it) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Split Easy AI", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

            Button(
                enabled = !authState.loading,
                onClick = {
                    coroutineScope.launch {
                        AuthManager.signIn(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        authState.loading -> MaterialTheme.colorScheme.surface
                        authState.error != null -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = when {
                        authState.loading -> MaterialTheme.colorScheme.onSurface
                        authState.error != null -> MaterialTheme.colorScheme.onError
                        else -> MaterialTheme.colorScheme.onPrimary
                    }
                )
            ) {
                when {
                    authState.loading -> {
                        Text("Please Wait...")
                    }
                    authState.error != null -> {
                        Text("Error: ${authState.error!!.localizedMessage}")
                    }
                    authState.user != null -> {
                        Text("Signed In as ${authState.user?.displayName}")
                    }
                    else -> {
                        Text("Sign In With Google")
                    }
                }
            }
        }
    }
}
