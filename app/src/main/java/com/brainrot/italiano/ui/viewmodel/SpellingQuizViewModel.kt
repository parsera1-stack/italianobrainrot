package com.brainrot.italiano.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainrot.italiano.domain.model.QuizQuestion
import com.brainrot.italiano.domain.usecase.CheckAnswerUseCase
import com.brainrot.italiano.domain.usecase.GenerateSpellingQuestionUseCase
import com.brainrot.italiano.domain.usecase.GetNextWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpellingQuizViewModel @Inject constructor(
    private val generateQuestion: GenerateSpellingQuestionUseCase,
    private val checkAnswer: CheckAnswerUseCase,
    private val getNextWord: GetNextWordUseCase
) : ViewModel() {

    private val _currentQuestion = MutableLiveData<QuizQuestion?>()
    val currentQuestion: LiveData<QuizQuestion?> = _currentQuestion

    private val _feedback = MutableLiveData<Feedback?>()
    val feedback: LiveData<Feedback?> = _feedback

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _totalQuestions = MutableLiveData(0)
    val totalQuestions: LiveData<Int> = _totalQuestions

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _isLoading.value = true
            _feedback.value = null

            val result = generateQuestion()
            result.onSuccess { question ->
                _currentQuestion.value = question
                _totalQuestions.value = (_totalQuestions.value ?: 0) + 1
            }.onFailure { error ->
                _feedback.value = Feedback.Error(error.message ?: "Ошибка загрузки")
            }

            _isLoading.value = false
        }
    }

    fun checkAnswer(selectedAnswer: String) {
        val question = _currentQuestion.value ?: return

        val isCorrect = normalizeAnswer(selectedAnswer) == normalizeAnswer(question.correctAnswer)

        if (isCorrect) {
            _score.value = (_score.value ?: 0) + 1
        }

        _feedback.value = Feedback.Answer(
            isCorrect = isCorrect,
            correctAnswer = question.correctAnswer,
            russianWord = question.russianWord
        )
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    /**
     * Нормализация ответа: нижний регистр, без пробелов, без артиклей
     */
    private fun normalizeAnswer(answer: String): String {
        return answer
            .lowercase()
            .replace("\s+".toRegex(), "")
            .replace("^(a|an|the)".toRegex(), "")
    }

    sealed class Feedback {
        data class Answer(
            val isCorrect: Boolean,
            val correctAnswer: String,
            val russianWord: String
        ) : Feedback()

        data class Error(val message: String) : Feedback()
    }
}
