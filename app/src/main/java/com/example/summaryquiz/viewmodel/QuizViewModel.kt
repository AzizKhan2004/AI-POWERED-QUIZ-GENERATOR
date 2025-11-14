package com.example.summaryquiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summaryquiz.data.QuizAttempt
import com.example.summaryquiz.data.QuizRepository
import com.example.summaryquiz.logic.QuestionGenerator
import com.example.summaryquiz.logic.Summarizer
import com.example.summaryquiz.model.QuizQuestion
import com.example.summaryquiz.ui.QuizUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<QuizAttempt>>(emptyList())
    val history: StateFlow<List<QuizAttempt>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            repository.attempts.collectLatest { attempts ->
                _history.value = attempts
            }
        }
    }

    fun updateInputText(text: String) {
        val cleanText = text.replace("\\s+".toRegex(), " ")
        val wordCount = cleanText.trim().split(" ").filter { it.isNotBlank() }.size
        _uiState.value = _uiState.value.copy(
            inputText = text,
            wordCount = wordCount,
            errorMessage = null
        )
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _uiState.value = _uiState.value.copy(
            answers = _uiState.value.answers + (questionIndex to optionIndex)
        )
    }

    fun generateQuiz() {
        val text = _uiState.value.inputText.trim()
        val wordCount = _uiState.value.wordCount

        if (wordCount < 200) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please provide at least 200 words (current: $wordCount)."
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)

                val summary = Summarizer.summarize(text)
                val questions = QuestionGenerator.generateQuestions(text, required = 10)

                val newAttempt = QuizAttempt(
                    originalText = text,
                    summary = summary,
                    questions = questions
                )
                repository.saveAttempt(newAttempt)

                _uiState.value = _uiState.value.copy(
                    summary = summary,
                    questions = questions,
                    answers = emptyMap(),
                    isGenerating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.localizedMessage ?: "Failed to generate quiz",
                    isGenerating = false
                )
            }
        }
    }

    fun resetCurrentQuiz() {
        _uiState.value = _uiState.value.copy(
            summary = "",
            questions = emptyList(),
            answers = emptyMap()
        )
    }
}

class QuizViewModelFactory(
    private val repository: QuizRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

