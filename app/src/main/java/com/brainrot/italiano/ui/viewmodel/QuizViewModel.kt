package com.brainrot.italiano.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainrot.italiano.domain.model.*
import com.brainrot.italiano.domain.usecase.CheckAnswerUseCase
import com.brainrot.italiano.domain.usecase.GenerateQuizQuestionUseCase
import com.brainrot.italiano.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizState {
    object Loading : QuizState()
    data class Question(val question: QuizQuestion, val character: Character) : QuizState()
    data class Correct(val character: Character) : QuizState()
    data class Wrong(val character: Character, val correctAnswer: String) : QuizState()
    object Finished : QuizState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateQuestion: GenerateQuizQuestionUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val repository: WordRepository
) : ViewModel() {

    private val _quizState = MutableLiveData<QuizState>(QuizState.Loading)
    val quizState: LiveData<QuizState> = _quizState

    private var currentLevel: Int = 1
    private var currentQuestion: QuizQuestion? = null
    private var currentCharacter: Character? = null

    fun startQuiz(level: Int) {
        currentLevel = level
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            val question = generateQuestion(currentLevel)
            val character = Characters.getRandom()

            if (question == null) {
                _quizState.value = QuizState.Finished
                return@launch
            }

            currentQuestion = question
            currentCharacter = character
            _quizState.value = QuizState.Question(question, character)
        }
    }

    fun submitAnswer(userAnswer: String) {
        val question = currentQuestion ?: return
        val character = currentCharacter ?: return

        val isCorrect = checkAnswer(
            userAnswer = userAnswer,
            correctAnswer = question.correctAnswer,
            specialNote = getSpecialNote(question.word.id)
        )

        viewModelScope.launch {
            repository.recordAnswer(
                wordId = question.word.id,
                isCorrect = isCorrect,
                level = currentLevel
            )

            if (isCorrect) {
                _quizState.value = QuizState.Correct(character)
            } else {
                _quizState.value = QuizState.Wrong(character, question.correctAnswer)
            }

            // Автоматический переход к следующему вопросу через 1.5 секунды
            delay(1500)
            loadNextQuestion()
        }
    }

    fun submitMultipleChoiceAnswer(selectedOption: String) {
        submitAnswer(selectedOption)
    }

    private fun getSpecialNote(wordId: Long): String? {
        // Стартовые слова с особыми правилами
        return when (wordId) {
            4L -> "принимаются оба варианта"  // dish
            13L, 14L -> "перевод всегда пианино"  // piano/pianos
            else -> null
        }
    }
}
