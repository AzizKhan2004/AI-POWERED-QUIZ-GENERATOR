package com.example.summaryquiz.ui

import com.example.summaryquiz.model.QuizQuestion

data class QuizUiState(
    val inputText: String = "",
    val summary: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val answers: Map<Int, Int> = emptyMap(),
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val wordCount: Int = 0
)

