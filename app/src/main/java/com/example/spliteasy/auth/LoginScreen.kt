package com.example.spliteasy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spliteasy.auth.AuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(onSignInSuccess: (user:FirebaseUser) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AuthManager.init(context)
        AuthManager.getCurrentUser()?.let { onSignInSuccess(it) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SplitEasy", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

                Button(
                    enabled = !isLoading,
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            val user = AuthManager.signIn(context)
                            isLoading = false
                            if (user != null) onSignInSuccess(user)
                            else errorMessage = "Sign-in failed"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.surface)
                    } else {
                        Text("Sign in with Google")
                    }
            }

            errorMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
