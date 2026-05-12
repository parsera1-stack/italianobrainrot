package com.brainrot.italiano.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brainrot.italiano.data.local.entity.DefaultWordEntity

@Dao
interface DefaultWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefaultWords(words: List<DefaultWordEntity>)

    @Query("SELECT * FROM default_words")
    suspend fun getAllDefaultWords(): List<DefaultWordEntity>

    @Query("SELECT COUNT(*) FROM default_words")
    suspend fun getCount(): Int
}
