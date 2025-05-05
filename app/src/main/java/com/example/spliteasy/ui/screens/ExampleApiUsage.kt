package com.example.spliteasy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spliteasy.api.ApiService
import com.example.spliteasy.auth.AuthManager
import org.json.JSONObject

/**
 * Example ViewModel that demonstrates how to use the ApiService
 */
class ExampleViewModel {
    private val apiService = ApiService()

    suspend fun getGroups(): Result<List<Group>> {
        return apiService.get("/groups").map { responseBody ->
            // Parse JSON response and convert to domain objects
            parseGroupsResponse(responseBody)
        }
    }

    suspend fun createGroup(name: String): Result<Group> {
        val requestBody = JSONObject().apply {
            put("name", name)
        }

        return apiService.post("/groups", requestBody).map { responseBody ->
            // Parse JSON response and convert to domain object
            parseGroupResponse(responseBody)
        }
    }

    suspend fun addGroupMember(groupId: String, userId: String): Result<Boolean> {
        val requestBody = JSONObject().apply {
            put("userId", userId)
        }

        return apiService.post("/groups/$groupId/members", requestBody).map {
            true // Success if we got here
        }
    }

    // Helper methods to parse API responses
    private fun parseGroupsResponse(response: String): List<Group> {
        // Implementation would depend on your actual API response format
        // This is just a placeholder
        val jsonArray = JSONObject(response).getJSONArray("groups")
        val groups = mutableListOf<Group>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            groups.add(Group(
                id = jsonObject.getString("id"),
                name = jsonObject.getString("name")
            ))
        }

        return groups
    }

    private fun parseGroupResponse(response: String): Group {
        // Implementation would depend on your actual API response format
        val jsonObject = JSONObject(response)
        return Group(
            id = jsonObject.getString("id"),
            name = jsonObject.getString("name")
        )
    }
}

/**
 * Simple data class representing a group
 */
data class Group(
    val id: String,
    val name: String
)

/**
 * Example Composable that demonstrates using the API through the ViewModel
 */
@Composable
fun GroupsScreen(viewModel: ExampleViewModel = viewModel()) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Make sure the user is signed in before making API calls
        if (AuthManager.getCurrentUser() == null) {
            // Try to sign in first
            val user = AuthManager.signIn(context)
            if (user == null) {
                error = "Authentication failed"
                isLoading = false
                return@LaunchedEffect
            }
        }

        // Now we can make the API call
        viewModel.getGroups()
            .onSuccess { result ->
                groups = result
                isLoading = false
            }
            .onFailure { exception ->
                error = exception.message ?: "Unknown error"
                isLoading = false
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }
        if (error != null) {
            Text(text = "Error: $error")
        }
        if (groups.isNotEmpty()) {
            Text(text = "Groups:")
            groups.forEach { group ->
                Text(text = group.name)
            }
        }
        else {
            Text(text = "No groups found")
        }
    }
}