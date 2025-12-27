package com.example.mana.ui.home.widgets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mana.ai.GenerativeAiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyOmenViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DailyOmenUiState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel by lazy {
        GenerativeAiManager.getActiveModel(application.applicationContext)
    }

    init {
        _uiState.update { it.copy(omen = "برای دریافت فال امروز، دکمه بروزرسانی را بزنید.") }
    }

    fun fetchDailyOmen() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val prompt = "یک فال روزانه حافظ برای امروز به زبان فارسی برای من بگیر و در یک پاراگراف کوتاه و دوستانه تفسیر کن."
                val response = generativeModel.generateContent(prompt)

                val sdf = SimpleDateFormat("HH:mm", Locale("fa"))
                val currentTime = sdf.format(Date())

                _uiState.update {
                    it.copy(
                        omen = response.text ?: "خطا در دریافت فال.",
                        isLoading = false,
                        lastUpdated = currentTime
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        omen = "متاسفانه خطایی در ارتباط با هوش مصنوعی رخ داد. لطفاً از فعال بودن اینترنت و صحت کلید API خود مطمئن شوید.",
                        isLoading = false
                    )
                }
            }
        }
    }
}
