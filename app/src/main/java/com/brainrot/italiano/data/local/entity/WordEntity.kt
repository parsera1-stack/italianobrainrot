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
     * Запятые внутри значений экранируются кавычками
     */
    fun toCsvLine(): String {
        val rus = escapeCsv(russian)
        val eng = escapeCsv(english)
        return "$rus,$eng,${if (isLearned) 1 else 0},$totalShows,$totalCorrect,$totalWrong"
    }

    companion object {
        fun fromCsvLine(line: String): WordEntity? {
            val parts = parseCsvLine(line)
            if (parts.size < 2) return null
            return WordEntity(
                russian = unescapeCsv(parts[0].trim()),
                english = unescapeCsv(parts[1].trim()).trimArticle(),
                isLearned = parts.getOrNull(2)?.trim() == "1",
                totalShows = parts.getOrNull(3)?.trim()?.toIntOrNull() ?: 0,
                totalCorrect = parts.getOrNull(4)?.trim()?.toIntOrNull() ?: 0,
                totalWrong = parts.getOrNull(5)?.trim()?.toIntOrNull() ?: 0
            )
        }

        private fun escapeCsv(value: String): String {
            return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                "\"" + value.replace("\"", "\"\"") + "\""
            } else {
                value
            }
        }

        private fun unescapeCsv(value: String): String {
            return if (value.startsWith("\"") && value.endsWith("\"")) {
                value.substring(1, value.length - 1).replace("\"\"", "\"")
            } else {
                value
            }
        }

        /**
         * Парсит CSV-строку с учетом кавычек
         */
        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            val current = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < line.length) {
                val char = line[i]
                when {
                    char == '"' -> {
                        inQuotes = !inQuotes
                    }
                    char == ',' && !inQuotes -> {
                        result.add(current.toString())
                        current.clear()
                    }
                    else -> {
                        current.append(char)
                    }
                }
                i++
            }
            result.add(current.toString())
            return result
        }

        private fun String.trimArticle(): String {
            return this.replace(Regex("^(a |an |the |A |An |The )"), "")
        }
    }
}
