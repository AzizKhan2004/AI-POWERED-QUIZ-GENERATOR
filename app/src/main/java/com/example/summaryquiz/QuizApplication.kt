package com.example.summaryquiz

import android.app.Application
import com.example.summaryquiz.data.QuizDatabase
import com.example.summaryquiz.data.QuizRepository

class QuizApplication : Application() {

    val repository: QuizRepository by lazy {
        val database = QuizDatabase.getDatabase(this)
        QuizRepository(database.quizAttemptDao())
    }
}

