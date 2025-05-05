package com.example.spliteasy.transaction

//import android.graphics.Bitmap
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.spliteasy.BuildConfig
//import com.example.spliteasy.api.Member
//import com.example.spliteasy.api.TransactionType
//import com.google.ai.client.generativeai.GenerativeModel
//import com.google.ai.client.generativeai.type.content
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class ExpenseViewModel : ViewModel() {
//
//    private val _transactionType = MutableStateFlow(TransactionType.Expense)
//    val transactionType: StateFlow<TransactionType> = _transactionType
//    private val _billImage = MutableStateFlow<Bitmap?>(null)
//    val billImage: StateFlow<Bitmap?> = _billImage
//    private val _aiResponse = MutableStateFlow("")
//    val aiResponse: StateFlow<String> = _aiResponse
//    private val _summaryData = MutableStateFlow("")
//    val summaryData: StateFlow<String> = _summaryData
//    private val _isProcessing = MutableStateFlow(false)
//    val isProcessing: StateFlow<Boolean> = _isProcessing
//
//    private val generativeModel = GenerativeModel(
//        modelName = "gemini-1.5-flash",
//        apiKey = BuildConfig.apiKey
//    )
//
//    fun setTransactionType(type: TransactionType) {
//        _transactionType.value = type
//    }
//
//    fun setBillImage(image: Bitmap) {
//        _billImage.value = image
//    }
//
//    fun resetBillImage() {
//        _billImage.value = null
//        _aiResponse.value = ""
//    }
//
//    fun resetAIResponse() {
//        _aiResponse.value = ""
//    }
//
//    fun resetSummary() {
//        _summaryData.value = ""
//    }
//
//    fun analyzeImage(prompt: String) {
//        val defaultPrompt = """
//            Please analyze this receipt and provide an itemized list of all items with their prices and quantities.
//            Also include the total (with tax/tip if visible). Format the output as a clear table.
//            If this is not a receipt, describe what's in the image.
//        """.trimIndent()
//
//        val actualPrompt = if (prompt.isBlank()) defaultPrompt else prompt
//
//        val image = _billImage.value ?: return
//
//        viewModelScope.launch(Dispatchers.IO) {
//            _isProcessing.value = true
//            try {
//                val response = generativeModel.generateContent(
//                    content {
//                        image(image)
//                        text(actualPrompt)
//                    }
//                )
//                _aiResponse.value = response.text ?: "No response from AI"
//            } catch (e: Exception) {
//                _aiResponse.value = "Error analyzing image: ${e.message ?: "Unknown error"}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun generateSplitStrategy(prompt: String, selectedMembers: List<Member>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _isProcessing.value = true
//
//            val membersText = if (selectedMembers.isNotEmpty()) {
//                selectedMembers.joinToString(", ") { it.user.name }
//            } else {
//                "All members"
//            }
//
//            val fullPrompt = """
//                Based on the following itemized bill and split strategy, suggest how much each participant should pay.
//
//                ### Bill:
//                ${_aiResponse.value}
//
//                ### Split Strategy:
//                $prompt
//
//                ### Members:
//                $membersText
//
//                Format clearly and list each participant's payment.
//            """.trimIndent()
//
//            try {
//                val response = generativeModel.generateContent(fullPrompt)
//                _aiResponse.value = response.text ?: "No response from AI"
//            } catch (e: Exception) {
//                _aiResponse.value = "Error generating split strategy: ${e.message ?: "Unknown error"}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun generateTransferDetails(prompt: String, selectedMembers: List<Member>) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _isProcessing.value = true
//
//            val membersText = if (selectedMembers.isNotEmpty()) {
//                selectedMembers.joinToString(", ") { it.user.name }
//            } else {
//                "All members"
//            }
//
//            val fullPrompt = """
//                Based on this split bill:
//
//                ${_aiResponse.value}
//
//                Please generate transfer details showing who owes whom and how much.
//
//                Members: $membersText
//            """.trimIndent()
//
//            try {
//                val response = generativeModel.generateContent(fullPrompt)
//                _aiResponse.value = response.text ?: "No response from AI"
//            } catch (e: Exception) {
//                _aiResponse.value = "Error generating transfer details: ${e.message ?: "Unknown error"}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//
//    fun generateSummary(prompt: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            _isProcessing.value = true
//
//            val fullPrompt = """
//                Please summarize the transaction below including date, type, total amount, and participants with individual amounts.
//
//                ### Transaction Type:
//                ${_transactionType.value}
//
//                ### Details:
//                ${_aiResponse.value}
//            """.trimIndent()
//
//            try {
//                val response = generativeModel.generateContent(fullPrompt)
//                _summaryData.value = response.text ?: "No response from AI"
//            } catch (e: Exception) {
//                _summaryData.value = "Error generating summary: ${e.message ?: "Unknown error"}"
//            } finally {
//                _isProcessing.value = false
//            }
//        }
//    }
//}
