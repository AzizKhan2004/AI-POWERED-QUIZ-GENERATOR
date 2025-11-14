package com.example.summaryquiz.logic

import java.util.Locale

object Summarizer {

    private val sentenceRegex = Regex("(?<=[.!?])\\s+")

    fun summarize(text: String, maxSentences: Int = 3): String {
        val cleaned = text.trim().replace("\\s+".toRegex(), " ")
        val sentences = cleaned.split(sentenceRegex).filter { it.length > 10 }

        if (sentences.size <= maxSentences) return sentences.joinToString(" ")

        val scores = scoreSentences(sentences)
        return sentences
            .zip(scores)
            .sortedByDescending { it.second }
            .take(maxSentences)
            .sortedBy { sentences.indexOf(it.first) }
            .joinToString(" ")
    }

    private fun scoreSentences(sentences: List<String>): List<Double> {
        val frequency = mutableMapOf<String, Int>()
        val stopWords = setOf(
            "the", "is", "are", "and", "a", "an", "to", "of", "in", "for",
            "on", "with", "as", "that", "this", "it", "by", "from", "at"
        )

        sentences.forEach { sentence ->
            sentence.split(" ").forEach { word ->
                val normalized = word
                    .lowercase(Locale.getDefault())
                    .replace("[^a-z0-9]".toRegex(), "")
                if (normalized.isNotBlank() && normalized !in stopWords) {
                    frequency[normalized] = frequency.getOrDefault(normalized, 0) + 1
                }
            }
        }

        val maxFrequency = frequency.values.maxOrNull()?.coerceAtLeast(1) ?: 1

        return sentences.map { sentence ->
            sentence.split(" ").sumOf { word ->
                val normalized = word
                    .lowercase(Locale.getDefault())
                    .replace("[^a-z0-9]".toRegex(), "")
                frequency.getOrDefault(normalized, 0).toDouble() / maxFrequency
            }
        }
    }
}

