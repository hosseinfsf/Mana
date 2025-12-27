package com.example.mana.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mana.ai.GenerativeAiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Participant { USER, MODEL }

data class ChatMessage(
    val text: String,
    val participant: Participant,
    val isLoading: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isModifyingText: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel by lazy {
        GenerativeAiManager.getActiveModel(application.applicationContext)
    }

    fun sendMessage(userMessage: String) {
        _uiState.update { it.copy(messages = it.messages + ChatMessage(userMessage, Participant.USER)) }
        generateResponse(userMessage)
    }

    private fun generateResponse(prompt: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(messages = it.messages + ChatMessage("", Participant.MODEL, isLoading = true)) }
                val response = generativeModel.generateContent(prompt)
                _uiState.update { currentState ->
                    val newMessages = currentState.messages.toMutableList()
                    newMessages[newMessages.lastIndex] = ChatMessage(response.text ?: "خطا", Participant.MODEL)
                    currentState.copy(messages = newMessages)
                }
            } catch (e: Exception) { /* ... */ }
        }
    }

    fun modifyText(text: String, action: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return
        _uiState.update { it.copy(isModifyingText = true) }
        viewModelScope.launch {
            try {
                val prompt = "Perform the following action on the text provided. Action: $action. Text: \"$text\""
                val response = generativeModel.generateContent(prompt)
                onResult(response.text ?: text) // return original text on error
            } catch (e: Exception) {
                onResult(text) // return original text on error
            } finally {
                 _uiState.update { it.copy(isModifyingText = false) }
            }
        }
    }
}
