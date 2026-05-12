package com.brainrot.italiano.domain.usecase

import android.content.Context
import android.net.Uri
import com.brainrot.italiano.domain.model.Word
import com.brainrot.italiano.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * UseCase для импорта слов из CSV
 * Формат: русский,английский (UTF-8, без заголовка)
 */
class ImportWordsFromCsvUseCase @Inject constructor(
    private val repository: WordRepository
) {

    suspend operator fun invoke(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val words = mutableListOf<Word>()
            var lineNumber = 0

            reader.useLines { lines ->
                lines.forEach { line ->
                    lineNumber++
                    if (line.isBlank()) return@forEach

                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        val russian = parts[0].trim()
                        val english = parts[1].trim().trimArticle()

                        if (russian.isNotBlank() && english.isNotBlank()) {
                            words.add(Word(russian = russian, english = english))
                        }
                    }
                }
            }

            words.forEach { word ->
                repository.addWord(word)
            }

            Result.success(words.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun String.trimArticle(): String {
        return this.replace(Regex("^(a |an |the |A |An |The )"), "")
    }
}
