package com.example.mana.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mana.ai.GenerativeAiManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Note(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    var summary: String? = null
)

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isSummarizing: Boolean = false
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState = _uiState.asStateFlow()

    private val generativeModel by lazy {
        GenerativeAiManager.getActiveModel(application.applicationContext)
    }

    // Simplified saving/loading for brevity
    private fun loadNotes() { /* ... */ }
    private fun saveNotes() { /* ... */ }

    fun addNote(title: String, content: String) {
        val newNote = Note(title = title, content = content)
        _uiState.update { it.copy(notes = it.notes + newNote) }
    }

    fun summarizeNote(noteId: Long) {
        val note = _uiState.value.notes.find { it.id == noteId } ?: return
        _uiState.update { it.copy(isSummarizing = true) }

        viewModelScope.launch {
            try {
                val prompt = "Summarize the following note in one short sentence: ${note.content}"
                val response = generativeModel.generateContent(prompt)
                val summary = response.text

                _uiState.update { currentState ->
                    val updatedNotes = currentState.notes.map {
                        if (it.id == noteId) it.copy(summary = summary)
                        else it
                    }
                    currentState.copy(notes = updatedNotes, isSummarizing = false)
                }
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(isSummarizing = false) }
            }
        }
    }
}
