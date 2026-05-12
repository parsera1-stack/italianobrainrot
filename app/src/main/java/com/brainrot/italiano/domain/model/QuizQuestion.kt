package com.brainrot.italiano.domain.model

/**
 * Модель вопроса для квиза
 */
data class QuizQuestion(
    val word: Word,
    val questionText: String,        // Текст вопроса (слово для перевода)
    val correctAnswer: String,       // Правильный ответ
    val options: List<String> = emptyList(), // Варианты для уровня 1
    val questionDirection: QuestionDirection,
    val questionType: QuestionType
)

enum class QuestionDirection {
    RUSSIAN_TO_ENGLISH,
    ENGLISH_TO_RUSSIAN
}

enum class QuestionType {
    MULTIPLE_CHOICE,    // Уровень 1 - выбор из 4 вариантов
    WRITTEN,           // Уровень 2 - письменный ввод
    MIXED              // Уровень 3 - смешанный
}
