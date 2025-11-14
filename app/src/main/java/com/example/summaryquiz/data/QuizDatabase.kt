package com.example.summaryquiz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [QuizAttempt::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(QuizConverters::class)
abstract class QuizDatabase : RoomDatabase() {

    abstract fun quizAttemptDao(): QuizAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "summary_quiz_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

