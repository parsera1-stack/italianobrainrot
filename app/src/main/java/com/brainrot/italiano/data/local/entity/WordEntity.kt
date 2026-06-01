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
        val rus = russian.escapeCsv()
        val eng = english.escapeCsv()
        return "$rus,$eng,${if (isLearned) 1 else 0},$totalShows,$totalCorrect,$totalWrong"
    }

    companion object {
        fun fromCsvLine(line: String): WordEntity? {
            // Парсим CSV с учётом кавычек
            val parts = parseCsvLine(line)
            if (parts.size < 2) return null
            return WordEntity(
                russian = parts[0].trim().unescapeCsv(),
                english = parts[1].trim().unescapeCsv().trimArticle(),
                isLearned = parts.getOrNull(2)?.trim() == "1",
                totalShows = parts.getOrNull(3)?.trim()?.toIntOrNull() ?: 0,
                totalCorrect = parts.getOrNull(4)?.trim()?.toIntOrNull() ?: 0,
                totalWrong = parts.getOrNull(5)?.trim()?.toIntOrNull() ?: 0
            )
        }

        /**
         * Парсит CSV-строку с учётом кавычек
         * "тарелка, блюдо",dish,0,0,0,0 → [тарелка, блюдо, dish, 0, 0, 0, 0]
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

        private fun String.escapeCsv(): String {
            return if (this.contains(",") || this.contains(""") || this.contains("
")) {
                """ + this.replace(""", """") + """
            } else {
                this
            }
        }

        private fun String.unescapeCsv(): String {
            return if (this.startsWith(""") && this.endsWith(""")) {
                this.substring(1, this.length - 1).replace("""", """)
            } else {
                this
            }
        }

        private fun String.trimArticle(): String {
            return this.replace(Regex("^(a |an |the |A |An |The )"), "")
        }
    }
}
