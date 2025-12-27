package com.example.mana.ui.clipboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mana.ai.GenerativeAiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClipboardAiState(
    val suggestion: String = "",
    val isLoading: Boolean = false
)

class ClipboardViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ClipboardAiState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel by lazy {
        GenerativeAiManager.getActiveModel(application.applicationContext)
    }

    fun processText(text: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val prompt = "Analyze the following text and suggest a primary action. For example, if it's a long article, suggest 'خلاصه کردن'. If it's a question, suggest 'پاسخ دادن'. If it's from a social media, suggest 'تولید پاسخ'. Keep the suggestion very short (one or two words). Text is: \"$text\""

                val response = generativeModel.generateContent(prompt)

                _uiState.update {
                    it.copy(
                        suggestion = response.text ?: "عملیات نامشخص",
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        suggestion = "خطا در پردازش",
                        isLoading = false
                    )
                }
            }
        }
    }
}
