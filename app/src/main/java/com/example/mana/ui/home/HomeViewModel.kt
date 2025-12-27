package com.example.mana.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mana.ai.GenerativeAiManager
import com.example.mana.ui.tasks.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "کاربر",
    val tasks: List<Task> = emptyList(),
    val shoppingItemCount: Int = 0,
    val nightSummary: String? = null,
    val isGeneratingSummary: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    
    private val generativeModel by lazy { GenerativeAiManager.getActiveModel(application.applicationContext) }

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            // Load User Name
            val userPrefs = getApplication<Application>().getSharedPreferences("mana_user_data", Context.MODE_PRIVATE)
            val userName = userPrefs.getString("USER_NAME", "کاربر") ?: "کاربر"

            // Load Tasks
            val tasksPrefs = getApplication<Application>().getSharedPreferences("mana_tasks", Context.MODE_PRIVATE)
            val tasksString = tasksPrefs.getString("daily_tasks", null)
            val tasks = if (tasksString != null) {
                tasksString.split(";").mapNotNull { 
                    val parts = it.split(",")
                    if (parts.size == 3) Task(parts[0].toLong(), parts[1], parts[2].toBoolean())
                    else null
                }
            } else emptyList()

            // Load Shopping List Count
            val shoppingPrefs = getApplication<Application>().getSharedPreferences("mana_shopping_list", Context.MODE_PRIVATE)
            val shoppingString = shoppingPrefs.getString("shopping_items", null)
            val shoppingItemCount = shoppingString?.split(";")?.filter { it.isNotBlank() }?.size ?: 0

            _uiState.update {
                it.copy(
                    userName = userName,
                    tasks = tasks, // Load all tasks
                    shoppingItemCount = shoppingItemCount
                )
            }
        }
    }

    fun generateNightSummary() {
        _uiState.update { it.copy(isGeneratingSummary = true) }
        viewModelScope.launch {
            val completedTasks = _uiState.value.tasks.filter { it.isCompleted }.joinToString(", ") { it.title }.ifBlank { "هیچ کاری" }
            val prompt = "Write a short, friendly, and poetic night summary (شب نامه) for a user named ${_uiState.value.userName}. Today they completed these tasks: $completedTasks. Also, mention that tomorrow is a new day and suggest a relaxing activity. Keep it under 60 words and use some emojis."
            try {
                val response = generativeModel.generateContent(prompt)
                _uiState.update { it.copy(isGeneratingSummary = false, nightSummary = response.text) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isGeneratingSummary = false, nightSummary = "خطا در تولید خلاصه شبانه.") }
            }
        }
    }
    
    fun clearNightSummary() {
         _uiState.update { it.copy(nightSummary = null) }
    }
}
