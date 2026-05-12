package com.brainrot.italiano.data.local.dao

import androidx.room.*
import com.brainrot.italiano.data.local.entity.WordAnswerHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordAnswerHistoryDao {

    @Insert
    suspend fun insertHistory(history: WordAnswerHistoryEntity)

    @Query("SELECT * FROM word_answers_history WHERE wordId = :wordId ORDER BY timestamp DESC LIMIT 5")
    fun getLastFiveAnswers(wordId: Long): Flow<List<WordAnswerHistoryEntity>>

    @Query("SELECT * FROM word_answers_history WHERE wordId = :wordId AND level = :level")
    fun getHistoryByLevel(wordId: Long, level: Int): Flow<List<WordAnswerHistoryEntity>>

    @Query("""
        SELECT COUNT(*) FROM word_answers_history 
        WHERE wordId = :wordId AND isCorrect = 1
    """)
    suspend fun getCorrectCount(wordId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM word_answers_history 
        WHERE wordId = :wordId AND isCorrect = 0
    """)
    suspend fun getWrongCount(wordId: Long): Int
}
