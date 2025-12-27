package com.example.mana.ui.onboarding

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class QuestionType {
    TEXT_INPUT,
    SINGLE_CHOICE
}

data class Question(
    val text: String,
    val type: QuestionType,
    val options: List<String> = emptyList()
)

data class OnboardingUiState(
    val currentQuestionIndex: Int = 0,
    val answers: Map<Int, String> = emptyMap(),
    val isCompleted: Boolean = false
)

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    private val userDataPrefs = application.getSharedPreferences("mana_user_data", Context.MODE_PRIVATE)

    val questions = listOf(
        Question("سلام! اسم کوچیکت چیه؟ چی صدات کنم؟ 😊", QuestionType.TEXT_INPUT),
        Question("چند سالته حدوداً؟", QuestionType.SINGLE_CHOICE, listOf("زیر ۱۸", "۱۸-۲۵", "۲۶-۳۵", "۳۶-۵۰", "بالای ۵۰")),
        Question("روزانه بیشتر چیکار می‌کنی؟", QuestionType.SINGLE_CHOICE, listOf("دانش‌آموز/جو", "شاغل", "خانه‌دار", "فریلنسر", "بازنشسته", "دیگر")),
        Question("ماه تولدت چیه؟", QuestionType.TEXT_INPUT),
        Question("کدوم شهر یا استان زندگی می‌کنی؟", QuestionType.TEXT_INPUT)
    )

    fun onAnswer(answer: String) {
        val currentIndex = _uiState.value.currentQuestionIndex
        _uiState.update {
            it.copy(answers = it.answers + (currentIndex to answer))
        }
    }

    fun onNext() {
        val nextIndex = _uiState.value.currentQuestionIndex + 1
        if (nextIndex < questions.size) {
            _uiState.update { it.copy(currentQuestionIndex = nextIndex) }
        } else {
            saveUserData()
            _uiState.update { it.copy(isCompleted = true) }
        }
    }

    private fun saveUserData() {
        with(userDataPrefs.edit()) {
            // Save user's name (answer to the first question)
            putString("USER_NAME", _uiState.value.answers[0])
            // Mark onboarding as complete
            putBoolean("ONBOARDING_COMPLETE", true)
            apply()
        }
    }
}
