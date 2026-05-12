package com.brainrot.italiano.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Стартовый словарь для инициализации БД
 */
@Entity(tableName = "default_words")
data class DefaultWordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val russian: String,
    val english: String,
    val specialNote: String? = null
)
