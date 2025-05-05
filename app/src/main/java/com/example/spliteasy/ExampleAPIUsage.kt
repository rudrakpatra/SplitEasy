package com.example.spliteasy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.spliteasy.api.ApiService
import com.example.spliteasy.auth.AuthManager
import com.example.spliteasy.ui.screens.ExampleViewModel
import com.example.spliteasy.ui.screens.GroupsScreen
import com.example.spliteasy.ui.theme.SplitEasyTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

class ExampleAPIUsageActivity : ComponentActivity() {
    // Initialize API service
    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AuthManager
        AuthManager.init(applicationContext)

        // Enable edge-to-edge rendering
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            SplitEasyTheme {
                // Use GroupsScreen which internally uses ExampleViewModel
                GroupsScreen()
            }
        }

        // Example of how to use ApiService directly from the Activity if needed
        demonstrateApiUsage()
    }

    private fun demonstrateApiUsage() {
        // Example of direct API usage from Activity
        lifecycleScope.launch {
            // First make sure user is authenticated
            if (AuthManager.getCurrentUser() == null) {
                val user = AuthManager.signIn(this@ExampleAPIUsageActivity)
                if (user == null) {
                    // Handle authentication failure
                    showToast("Authentication failed")
                    return@launch
                }
            }

            // Example API call to get groups
            apiService.get("/groups").onSuccess { response ->
                // Process successful response
                println("Groups response: $response")
            }.onFailure { exception ->
                // Handle error
                showToast("API error: ${exception.message}")
            }

            // Example API call to create a group
            val groupData = JSONObject().apply {
                put("name", "New Vacation Group")
            }

            apiService.post("/groups", groupData).onSuccess { response ->
                // Process successful response
                val groupId = JSONObject(response).getString("id")
                println("Created group with ID: $groupId")

                // Example of adding a member to the newly created group
                val memberData = JSONObject().apply {
                    put("userId", AuthManager.getCurrentUser()?.uid)
                }

                apiService.post("/groups/$groupId/members", memberData).onSuccess {
                    println("Successfully added member to group")
                }
            }
        }
    }

    private fun showToast(message: String) {
        // Implementation of showing a toast message
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}