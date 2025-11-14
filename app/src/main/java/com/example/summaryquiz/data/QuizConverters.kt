package com.example.summaryquiz.data

import androidx.room.TypeConverter
import com.example.summaryquiz.model.QuizQuestion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class QuizConverters {
    @TypeConverter
    fun fromQuestions(value: List<QuizQuestion>): String =
        Json.encodeToString(value)

    @TypeConverter
    fun toQuestions(value: String): List<QuizQuestion> =
        Json.decodeFromString(value)
}

