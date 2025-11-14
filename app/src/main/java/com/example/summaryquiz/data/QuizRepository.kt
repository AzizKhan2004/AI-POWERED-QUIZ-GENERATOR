package com.example.summaryquiz.data

class QuizRepository(private val dao: QuizAttemptDao) {

    val attempts = dao.observeAttempts()

    suspend fun saveAttempt(attempt: QuizAttempt) {
        dao.insertAttempt(attempt)
    }
}

