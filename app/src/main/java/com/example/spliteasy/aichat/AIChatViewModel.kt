package com.example.spliteasy.aichat

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spliteasy.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val imageAttachment: Bitmap? = null
)

sealed class ChatUiState {
    object Initial : ChatUiState()
    object Loading : ChatUiState()
    data class Success(val message: Message) : ChatUiState()
    data class Error(val errorMessage: String) : ChatUiState()
}

class AIChatViewModel(private val initialPrompt: String="You are a helpful assistant") : ViewModel() {
    private var _hasAttachedImage = MutableStateFlow(false)
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
        // Send initial prompt to the model
        sendInitialPrompt()
    }

    private fun sendInitialPrompt() {
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(initialPrompt)
                response.text?.let { outputContent ->
                    val botMessage = Message(
                        content = outputContent,
                        isFromUser = false
                    )
                    _messages.value = _messages.value + botMessage
                    _uiState.value = ChatUiState.Success(botMessage)
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.localizedMessage ?: "Failed to initialize chat")
            }
        }
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
        _messages.value = _messages.value + userMessageObj

        // Set loading state
        _uiState.value = ChatUiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = if (currentImage != null) {
                    // If there's an image, include it in the prompt
                    generativeModel.generateContent(
                        content {
                            image(currentImage!!)
                            text(if (userMessage.isBlank()) "What can you tell me about this image?" else userMessage)
                        }
                    )
                } else {
                    // Text-only prompt
                    generativeModel.generateContent(userMessage)
                }

                response.text?.let { outputContent ->
                    val botMessage = Message(
                        content = outputContent,
                        isFromUser = false
                    )
                    _messages.value = _messages.value + botMessage
                    _uiState.value = ChatUiState.Success(botMessage)
                }

                // Clear the current image after sending
                currentImage = null

            } catch (e: Exception) {
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
    fun getAttachedImage(): Bitmap? {
        return currentImage
    }
}