package com.example.mana.ui.tasks

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Task(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    var isCompleted: Boolean = false
)

data class DailyTasksUiState(
    val tasks: List<Task> = emptyList()
)

class DailyTasksViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DailyTasksUiState())
    val uiState = _uiState.asStateFlow()

    private val sharedPref = application.getSharedPreferences("mana_tasks", Context.MODE_PRIVATE)

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            val tasksString = sharedPref.getString("daily_tasks", null)
            if (tasksString != null) {
                val tasks = tasksString.split(";").mapNotNull { 
                    val parts = it.split(",")
                    if (parts.size == 3) Task(parts[0].toLong(), parts[1], parts[2].toBoolean())
                    else null
                }
                _uiState.update { it.copy(tasks = tasks) }
            }
        }
    }

    private fun saveTasks() {
        val tasksString = _uiState.value.tasks.joinToString(";") { "${it.id},${it.title},${it.isCompleted}" }
        with(sharedPref.edit()) {
            putString("daily_tasks", tasksString)
            apply()
        }
    }

    fun addTask(title: String) {
        val newTask = Task(title = title)
        _uiState.update { it.copy(tasks = it.tasks + newTask) }
        saveTasks()
    }

    fun toggleTaskCompletion(taskId: Long) {
        _uiState.update { currentState ->
            val updatedTasks = currentState.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted)
                else it
            }
            currentState.copy(tasks = updatedTasks)
        }
        saveTasks()
    }
    
    fun removeTask(taskId: Long) {
        _uiState.update { it.copy(tasks = it.tasks.filterNot { task -> task.id == taskId }) }
        saveTasks()
    }
}
