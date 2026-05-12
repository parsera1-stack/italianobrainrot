package com.brainrot.italiano.data.local.dao

import androidx.room.*
import com.brainrot.italiano.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE isLearned = 0")
    fun getActiveWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWordById(wordId: Long): WordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Delete
    suspend fun deleteWord(word: WordEntity)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordsCount(): Int

    @Query("SELECT * FROM words WHERE isLearned = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomActiveWords(limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE isLearned = 0 AND id != :excludeId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomDistractors(excludeId: Long, limit: Int): List<WordEntity>
}
