package com.brainrot.italiano.domain.usecase

import javax.inject.Inject

/**
 * UseCase для проверки ответа пользователя
 */
class CheckAnswerUseCase @Inject constructor() {

    /**
     * Проверяет ответ пользователя
     * @param userAnswer Ответ пользователя
     * @param correctAnswer Правильный ответ из БД
     * @param specialNote Особое примечание (для синонимов)
     * @return true если ответ правильный
     */
    operator fun invoke(userAnswer: String, correctAnswer: String, specialNote: String? = null): Boolean {
        val normalizedUser = normalizeAnswer(userAnswer)
        val normalizedCorrect = normalizeAnswer(correctAnswer)

        // Прямое совпадение
        if (normalizedUser == normalizedCorrect) return true

        // Проверка с заменой ё→е (осёл = осел)
        if (normalizedUser.replaceYo() == normalizedCorrect.replaceYo()) return true

        // Проверка синонимов (только для dish)
        if (specialNote != null && specialNote.contains("оба варианта")) {
            val synonyms = listOf("тарелка", "блюдо")
            val userNormalized = normalizedUser.replaceYo()
            if (synonyms.any { it.replaceYo() == userNormalized }) return true
        }

        // Для piano/pianos — всегда "пианино"
        if (specialNote != null && specialNote.contains("всегда пианино")) {
            if (normalizedUser.replaceYo() == "пианино") return true
        }

        return false
    }

    /**
     * Нормализация ответа:
     * - lower case
     * - trim пробелы
     * - удаление артиклей
     */
    private fun normalizeAnswer(answer: String): String {
        return answer
            .trim()
            .lowercase()
            .replace(Regex("^(a |an |the )"), "")
    }

    /**
     * Заменяет ё на е для сравнения
     * осёл → осел, ёж → еж
     */
    private fun String.replaceYo(): String {
        return this.replace('ё', 'е')
    }
}
