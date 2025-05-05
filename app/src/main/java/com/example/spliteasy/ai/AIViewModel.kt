package com.example.spliteasy.ai

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spliteasy.BuildConfig
import com.example.spliteasy.auth.AuthManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Message(
    val content: String,
    val isFromUser: Boolean,
    val imageAttachment: Bitmap? = null
)

sealed class ChatUiState {
    data object Initial : ChatUiState()
    data object Loading : ChatUiState()
    data class Success(val message: Message) : ChatUiState()
    data class Error(val errorMessage: String) : ChatUiState()
}


class AIViewModel() : ViewModel() {
    private val _hasAttachedImage = MutableStateFlow(false)
    val hasAttachedImage: StateFlow<Boolean> = _hasAttachedImage.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentImage: Bitmap? = null

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    init {
        // We'll start the session when the ViewModel is created
            startSession()
    }

    private fun startSession() {
        // Set loading state first
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch {
            try {
                // Use the injected user or get the current user
                val currentUser = AuthManager.getCurrentUser() ?: throw Exception("User not logged in")

                val prompt = "You are a helpful assistant to track expenses of the user ${currentUser.displayName}. " +
                        "You can access to firebase collection users/${currentUser.uid} to maintain this users data. " +
                        "Greet the user and ask him various options to update the expenses"

                // Generate the welcome message
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }

                response.text?.let { outputContent ->
                    val botMessage = Message(
                        content = outputContent,
                        isFromUser = false
                    )

                    // Update messages list with the new bot message
                    withContext(Dispatchers.Main) {
                        // Create a new list with the welcome message to avoid mutation issues
                        val updatedMessages = _messages.value.toMutableList().apply {
                            add(botMessage)
                        }
                        _messages.value = updatedMessages

                        // Update UI state
                        _uiState.value = ChatUiState.Success(botMessage)
                    }
                } ?: throw Exception("Empty response from AI")

            } catch (e: Exception) {
                Log.e("AIViewModel", "Error starting session", e)
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "Failed to initialize chat")
            }
        }
    }

    fun endSession() {
        // Clear all messages when ending the session
        _messages.value = emptyList()
        _uiState.value = ChatUiState.Initial
        clearImage()
    }

    fun sendMessage(userMessage: String) {
        // Don't send if both message is blank and no image
        if (userMessage.isBlank() && currentImage == null) return

        // Create message object with current text and image
        val userMessageObj = Message(
            content = userMessage,
            isFromUser = true,
            imageAttachment = currentImage
        )

        // Add user message to chat
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessageObj)
        _messages.value = currentMessages

        // Set loading state
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch {
            try {
                val response = if (currentImage != null) {
                    // If there's an image, include it in the prompt
                    withContext(Dispatchers.IO) {
                        generativeModel.generateContent(
                            content {
                                image(currentImage!!)
                                text(userMessage.ifBlank { "What can you tell me about this image?" })
                            }
                        )
                    }
                } else {
                    // Text-only prompt
                    withContext(Dispatchers.IO) {
                        generativeModel.generateContent(userMessage)
                    }
                }

                response.text?.let { outputContent ->
                    val botMessage = Message(
                        content = outputContent,
                        isFromUser = false
                    )

                    // Update messages list with the new bot message
                    val updatedMessages = _messages.value.toMutableList().apply {
                        add(botMessage)
                    }
                    _messages.value = updatedMessages

                    // Update UI state
                    _uiState.value = ChatUiState.Success(botMessage)
                }

                // Clear the current image after sending
                clearImage()

            } catch (e: Exception) {
                Log.e("AIViewModel", "Error sending message", e)
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "Failed to get response")
            }
        }
    }

    fun setImage(bitmap: Bitmap) {
        currentImage = bitmap
        _hasAttachedImage.value = true
    }

    fun clearImage() {
        currentImage = null
        _hasAttachedImage.value = false
    }

    fun hasAttachedImage(): Boolean {
        return currentImage != null
    }

    // Add getter for attached image to use in UI
    fun getAttachedImageBitmap(): Bitmap? {
        return currentImage
    }
}