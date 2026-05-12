package com.brainrot.italiano.domain.usecase

import com.brainrot.italiano.domain.model.*
import com.brainrot.italiano.data.repository.WordRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * UseCase для генерации вопроса квиза
 */
class GenerateQuizQuestionUseCase @Inject constructor(
    private val repository: WordRepository,
    private val getNextWord: GetNextWordUseCase
) {

    suspend operator fun invoke(level: Int): QuizQuestion? {
        val word = getNextWord() ?: return null

        // Определяем направление перевода (50/50)
        val direction = if (Random.nextBoolean()) {
            QuestionDirection.RUSSIAN_TO_ENGLISH
        } else {
            QuestionDirection.ENGLISH_TO_RUSSIAN
        }

        val questionType = when (level) {
            1 -> QuestionType.MULTIPLE_CHOICE
            2 -> QuestionType.WRITTEN
            3 -> if (Random.nextBoolean()) QuestionType.MULTIPLE_CHOICE else QuestionType.WRITTEN
            else -> QuestionType.MULTIPLE_CHOICE
        }

        val questionText = if (direction == QuestionDirection.RUSSIAN_TO_ENGLISH) {
            word.russian
        } else {
            word.english
        }

        val correctAnswer = if (direction == QuestionDirection.RUSSIAN_TO_ENGLISH) {
            word.english
        } else {
            word.russian
        }

        val options = if (questionType == QuestionType.MULTIPLE_CHOICE) {
            generateOptions(word, direction)
        } else {
            emptyList()
        }

        return QuizQuestion(
            word = word,
            questionText = questionText,
            correctAnswer = correctAnswer,
            options = options,
            questionDirection = direction,
            questionType = questionType
        )
    }

    private suspend fun generateOptions(word: Word, direction: QuestionDirection): List<String> {
        val distractors = repository.getRandomDistractors(word.id, 3)
        val correct = if (direction == QuestionDirection.RUSSIAN_TO_ENGLISH) word.english else word.russian

        val options = mutableListOf(correct)

        // Добавляем дистракторы из словаря пользователя
        distractors.forEach { distractor ->
            val option = if (direction == QuestionDirection.RUSSIAN_TO_ENGLISH) {
                distractor.english
            } else {
                distractor.russian
            }
            if (option != correct && !options.contains(option)) {
                options.add(option)
            }
        }

        // Если не хватило уникальных, добавляем случайные из стартового набора
        while (options.size < 4) {
            val randomWord = repository.getRandomActiveWords(1).firstOrNull() ?: break
            val option = if (direction == QuestionDirection.RUSSIAN_TO_ENGLISH) {
                randomWord.english
            } else {
                randomWord.russian
            }
            if (option != correct && !options.contains(option)) {
                options.add(option)
            }
        }

        return options.shuffled()
    }
}
