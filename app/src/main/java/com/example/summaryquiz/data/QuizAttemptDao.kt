package com.example.summaryquiz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttempt)

    @Query("SELECT * FROM quiz_attempts ORDER BY createdAt DESC")
    fun observeAttempts(): Flow<List<QuizAttempt>>
}

