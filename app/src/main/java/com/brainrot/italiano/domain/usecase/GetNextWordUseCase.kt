package com.brainrot.italiano.domain.usecase

import com.brainrot.italiano.domain.model.Word
import com.brainrot.italiano.data.repository.WordRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * UseCase для выбора следующего слова по алгоритму веса
 * weight = 1.0 + (consecutiveWrong * 0.5) + (daysSinceLastShown * 0.2)
 */
class GetNextWordUseCase @Inject constructor(
    private val repository: WordRepository
) {

    suspend operator fun invoke(): Word? {
        val words = repository.getRandomActiveWords(50) // Берём больше для случайности
        if (words.isEmpty()) return null

        // Рассчитываем вес для каждого слова
        val weightedWords = words.map { word ->
            val daysSinceLastShown = calculateDaysSince(word.lastShownTimestamp)
            val cappedConsecutiveWrong = minOf(word.consecutiveWrong, 5)
            val cappedDays = minOf(daysSinceLastShown, 7)

            val weight = 1.0 +
                    (cappedConsecutiveWrong * 0.5) +
                    (cappedDays * 0.2)

            word to weight
        }

        // Сортируем по весу (убывание) и берём топ-5
        val topWords = weightedWords
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }

        // Случайно выбираем одно из топ-5
        return topWords.random()
    }

    private fun calculateDaysSince(timestamp: Long): Long {
        if (timestamp == 0L) return 7 // Если слово ещё не показывалось - максимальный приоритет
        val diff = System.currentTimeMillis() - timestamp
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}
