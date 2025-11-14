package com.example.summaryquiz.logic

import com.example.summaryquiz.model.QuizQuestion
import kotlin.random.Random

object QuestionGenerator {

    fun generateQuestions(source: String, required: Int = 10): List<QuizQuestion> {
        val sentences = source
            .split(Regex("(?<=[.!?])\\s+"))
            .filter { it.length > 25 }
            .toMutableList()

        if (sentences.isEmpty()) {
            sentences += source
        }

        val keywords = extractKeywords(source)
        val questions = mutableListOf<QuizQuestion>()

        var sentenceIndex = 0
        while (questions.size < required) {
            val sentence = sentences[sentenceIndex % sentences.size]
            val keyword = keywords.getOrNull(sentenceIndex % keywords.size)
                ?: sentence.split(" ").maxByOrNull { it.length } ?: "concept"

            val sanitizedKeyword = keyword.replace("[^A-Za-z0-9]".toRegex(), "")
            val blankSentence = sentence.replace(keyword, "_____")

            val distractors = keywords
                .filter { it != keyword }
                .shuffled()
                .take(3)
                .ifEmpty {
                    listOf("Context", "Detail", "Idea")
                }

            val options = (distractors + sanitizedKeyword).shuffled(Random(sentenceIndex))

            questions += QuizQuestion(
                prompt = "Fill in the blank: $blankSentence",
                options = options,
                answerIndex = options.indexOf(sanitizedKeyword)
            )
            sentenceIndex++
        }

        return questions
    }

    private fun extractKeywords(text: String): List<String> {
        return text
            .lowercase()
            .split("\\s+".toRegex())
            .map { it.replace("[^A-Za-z0-9]".toRegex(), "") }
            .filter { it.length > 5 }
            .distinct()
            .take(30)
    }
}

