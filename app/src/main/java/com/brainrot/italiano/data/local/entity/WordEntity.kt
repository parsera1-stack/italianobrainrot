package com.brainrot.italiano.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Основная таблица слов для изучения
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
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
) {
    /**
     * Формат CSV: russian,english,isLearned,totalShows,totalCorrect,totalWrong
     */
    fun toCsvLine(): String {
        return "$russian,$english,${if (isLearned) 1 else 0},$totalShows,$totalCorrect,$totalWrong"
    }

    companion object {
        fun fromCsvLine(line: String): WordEntity? {
            val parts = line.split(",")
            if (parts.size < 2) return null
            return WordEntity(
                russian = parts[0].trim(),
                english = parts[1].trim().trimArticle(),
                isLearned = parts.getOrNull(2)?.trim() == "1",
                totalShows = parts.getOrNull(3)?.trim()?.toIntOrNull() ?: 0,
                totalCorrect = parts.getOrNull(4)?.trim()?.toIntOrNull() ?: 0,
                totalWrong = parts.getOrNull(5)?.trim()?.toIntOrNull() ?: 0
            )
        }

        private fun String.trimArticle(): String {
            return this.replace(Regex("^(a |an |the |A |An |The )"), "")
        }
    }
}
