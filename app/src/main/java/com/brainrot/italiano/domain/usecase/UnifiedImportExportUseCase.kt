package com.brainrot.italiano.domain.usecase

import android.content.Context
import android.net.Uri
import com.brainrot.italiano.data.local.entity.WordEntity
import com.brainrot.italiano.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * UseCase for unified import/export of words and statistics to CSV
 * Format: russian,english,isLearned,totalShows,totalCorrect,totalWrong
 */
class UnifiedImportExportUseCase @Inject constructor(
    private val repository: WordRepository
) {

    /**
     * Export all words with statistics to CSV
     */
    suspend fun exportToCsv(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val words = repository.getAllWords().first()

            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot create file"))

            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                // Header
                writer.write("russian,english,isLearned,totalShows,totalCorrect,totalWrong")
                writer.write(System.lineSeparator())

                // Data
                words.forEach { word ->
                    val entity = WordEntity(
                        id = word.id,
                        russian = word.russian,
                        english = word.english,
                        isLearned = word.isLearned,
                        totalShows = word.totalShows,
                        totalCorrect = word.totalCorrect,
                        totalWrong = word.totalWrong,
                        consecutiveWrong = word.consecutiveWrong,
                        lastShownTimestamp = word.lastShownTimestamp,
                        lastResultCorrect = word.lastResultCorrect
                    )
                    writer.write(entity.toCsvLine())
                    writer.write(System.lineSeparator())
                }
            }

            Result.success(words.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import words with statistics from CSV
     * Overwrites existing words or adds new ones
     */
    suspend fun importFromCsv(context: Context, uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open file"))

            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            var imported = 0
            var updated = 0
            var skipped = 0

            reader.useLines { lines ->
                lines.forEachIndexed { index, line ->
                    if (index == 0 && line.lowercase().contains("russian")) {
                        // Skip header
                        return@forEachIndexed
                    }
                    if (line.isBlank()) return@forEachIndexed

                    val entity = WordEntity.fromCsvLine(line)
                    if (entity != null) {
                        // Check if word already exists
                        val existingWords = repository.getAllWords().first()
                        val existing = existingWords.find { 
                            it.russian == entity.russian && it.english == entity.english 
                        }

                        if (existing != null) {
                            // Update statistics
                            repository.updateWord(existing.copy(
                                isLearned = entity.isLearned,
                                totalShows = entity.totalShows,
                                totalCorrect = entity.totalCorrect,
                                totalWrong = entity.totalWrong
                            ))
                            updated++
                        } else {
                            // Add new word
                            repository.addWord(com.brainrot.italiano.domain.model.Word(
                                russian = entity.russian,
                                english = entity.english,
                                isLearned = entity.isLearned,
                                totalShows = entity.totalShows,
                                totalCorrect = entity.totalCorrect,
                                totalWrong = entity.totalWrong
                            ))
                            imported++
                        }
                    } else {
                        skipped++
                    }
                }
            }

            Result.success(ImportResult(imported, updated, skipped))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class ImportResult(
        val imported: Int,
        val updated: Int,
        val skipped: Int
    ) {
        override fun toString(): String {
            return "Added: $imported, Updated: $updated, Skipped: $skipped"
        }
    }
}
