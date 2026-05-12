package com.brainrot.italiano.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * История ответов по каждому слову
 */
@Entity(
    tableName = "word_answers_history",
    primaryKeys = ["wordId", "timestamp"],
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"])]
)
data class WordAnswerHistoryEntity(
    val wordId: Long,
    val timestamp: Long,
    val isCorrect: Boolean,
    val level: Int // 1, 2 или 3
)
