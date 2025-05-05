package com.example.spliteasy.api

import com.example.spliteasy.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiService {
    private val client = OkHttpClient()
    private val baseUrl = "https://your-api-base-url.com/api" // Replace with your actual API base URL

    /**
     * Gets a fresh Firebase ID token to use for authentication
     */
    private suspend fun getIdToken(): String? {
        return try {
            AuthManager.getCurrentUser()?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Makes a GET request to the specified endpoint with authentication
     */
    suspend fun get(endpoint: String): Result<String> = withContext(Dispatchers.IO) {
        val token = getIdToken() ?: return@withContext Result.failure(
            IOException("Not authenticated")
        )

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(IOException("API call failed with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Makes a POST request to the specified endpoint with authentication and JSON body
     */
    suspend fun post(endpoint: String, jsonBody: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        val token = getIdToken() ?: return@withContext Result.failure(
            IOException("Not authenticated")
        )

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(IOException("API call failed with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Makes a PUT request to the specified endpoint with authentication and JSON body
     */
    suspend fun put(endpoint: String, jsonBody: JSONObject): Result<String> = withContext(Dispatchers.IO) {
        val token = getIdToken() ?: return@withContext Result.failure(
            IOException("Not authenticated")
        )

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Authorization", "Bearer $token")
            .put(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(IOException("API call failed with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Makes a DELETE request to the specified endpoint with authentication
     */
    suspend fun delete(endpoint: String): Result<String> = withContext(Dispatchers.IO) {
        val token = getIdToken() ?: return@withContext Result.failure(
            IOException("Not authenticated")
        )

        val request = Request.Builder()
            .url("$baseUrl$endpoint")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(IOException("API call failed with code: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}