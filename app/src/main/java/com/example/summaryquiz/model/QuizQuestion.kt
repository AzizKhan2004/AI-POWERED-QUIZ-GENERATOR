package com.example.summaryquiz.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val prompt: String,
    val options: List<String>,
    val answerIndex: Int
)

