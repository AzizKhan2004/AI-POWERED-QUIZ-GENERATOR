package com.example.summaryquiz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.summaryquiz.model.QuizQuestion

@Entity(tableName = "quiz_attempts")
data class QuizAttempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalText: String,
    val summary: String,
    val questions: List<QuizQuestion>,
    val createdAt: Long = System.currentTimeMillis()
)

