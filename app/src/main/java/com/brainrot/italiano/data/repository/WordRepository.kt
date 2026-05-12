package com.brainrot.italiano.data.repository

import com.brainrot.italiano.data.local.dao.DefaultWordDao
import com.brainrot.italiano.data.local.dao.WordAnswerHistoryDao
import com.brainrot.italiano.data.local.dao.WordDao
import com.brainrot.italiano.data.local.entity.WordAnswerHistoryEntity
import com.brainrot.italiano.data.local.entity.WordEntity
import com.brainrot.italiano.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(
    private val wordDao: WordDao,
    private val historyDao: WordAnswerHistoryDao,
    private val defaultWordDao: DefaultWordDao
) {

    fun getActiveWords(): Flow<List<Word>> {
        return wordDao.getActiveWords().map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getAllWords(): Flow<List<Word>> {
        return wordDao.getAllWords().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getWordById(wordId: Long): Word? {
        return wordDao.getWordById(wordId)?.toDomain()
    }

    suspend fun addWord(word: Word): Long {
        return wordDao.insertWord(word.toEntity())
    }

    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
    }

    suspend fun deleteWord(word: Word) {
        wordDao.deleteWord(word.toEntity())
    }

    suspend fun initializeWordsFromDefault() {
        val count = wordDao.getWordsCount()
        if (count == 0) {
            val defaults = defaultWordDao.getAllDefaultWords()
            val words = defaults.map { default ->
                WordEntity(
                    russian = default.russian,
                    english = default.english.trimArticle()
                )
            }
            wordDao.insertWords(words)
        }
    }

    suspend fun getRandomDistractors(excludeId: Long, limit: Int): List<Word> {
        return wordDao.getRandomDistractors(excludeId, limit).map { it.toDomain() }
    }

    suspend fun getRandomActiveWords(limit: Int): List<Word> {
        return wordDao.getRandomActiveWords(limit).map { it.toDomain() }
    }

    suspend fun recordAnswer(wordId: Long, isCorrect: Boolean, level: Int) {
        val word = wordDao.getWordById(wordId) ?: return

        val updatedWord = word.copy(
            totalShows = word.totalShows + 1,
            totalCorrect = if (isCorrect) word.totalCorrect + 1 else word.totalCorrect,
            totalWrong = if (!isCorrect) word.totalWrong + 1 else word.totalWrong,
            consecutiveWrong = if (!isCorrect) minOf(word.consecutiveWrong + 1, 5) else 0,
            lastShownTimestamp = System.currentTimeMillis(),
            lastResultCorrect = isCorrect
        )
        wordDao.updateWord(updatedWord)

        historyDao.insertHistory(
            WordAnswerHistoryEntity(
                wordId = wordId,
                timestamp = System.currentTimeMillis(),
                isCorrect = isCorrect,
                level = level
            )
        )
    }

    suspend fun getWordsCount(): Int = wordDao.getWordsCount()

    // Mappers
    private fun WordEntity.toDomain(): Word = Word(
        id = id,
        russian = russian,
        english = english,
        isLearned = isLearned,
        totalShows = totalShows,
        totalCorrect = totalCorrect,
        totalWrong = totalWrong,
        consecutiveWrong = consecutiveWrong,
        lastShownTimestamp = lastShownTimestamp,
        lastResultCorrect = lastResultCorrect
    )

    private fun Word.toEntity(): WordEntity = WordEntity(
        id = id,
        russian = russian,
        english = english.trimArticle(),
        isLearned = isLearned,
        totalShows = totalShows,
        totalCorrect = totalCorrect,
        totalWrong = totalWrong,
        consecutiveWrong = consecutiveWrong,
        lastShownTimestamp = lastShownTimestamp,
        lastResultCorrect = lastResultCorrect
    )

    /**
     * Удаляет артикли из начала строки
     */
    private fun String.trimArticle(): String {
        return this.replace(Regex("^(a |an |the |A |An |The )"), "")
    }
}
