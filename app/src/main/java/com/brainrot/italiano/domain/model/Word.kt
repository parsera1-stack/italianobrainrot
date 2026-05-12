package com.brainrot.italiano.domain.model

/**
 * Доменная модель слова
 */
data class Word(
    val id: Long = 0,
    val russian: String,
    val english: String,
    val isLearned: Boolean = false,
    val totalShows: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val consecutiveWrong: Int = 0,
    val lastShownTimestamp: Long = 0,
    val lastResultCorrect: Boolean = true
)
