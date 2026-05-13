package com.brainrot.italiano.ui.screens.quiz

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentQuizBinding
import com.brainrot.italiano.databinding.OverlayFeedbackBinding
import com.brainrot.italiano.domain.model.Character
import com.brainrot.italiano.domain.model.QuizQuestion
import com.brainrot.italiano.ui.viewmodel.QuizState
import com.brainrot.italiano.ui.viewmodel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by viewModels()
    private val args: QuizFragmentArgs by navArgs()

    private var _overlayBinding: OverlayFeedbackBinding? = null
    private val overlayBinding get() = _overlayBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        _overlayBinding = OverlayFeedbackBinding.bind(binding.root.findViewById(R.id.overlayFeedback))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startQuiz(args.level)
        observeQuizState()
        setupInputListeners()
        setupExitButton()
        setupOverlayButton()
    }

    private fun setupExitButton() {
        binding.btnExit.setOnClickListener {
            hideKeyboard()
            findNavController().popBackStack()
        }
    }

    private fun setupOverlayButton() {
        overlayBinding.btnOverlayNext.setOnClickListener {
            hideOverlay()
            viewModel.loadNextQuestion()
        }
    }

    private fun observeQuizState() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizState.Loading -> showLoading()
                is QuizState.Question -> showQuestion(state.question, state.character)
                is QuizState.Correct -> showCorrectOverlay(state.character)
                is QuizState.Wrong -> showWrongOverlay(state.character, state.correctAnswer)
                is QuizState.Finished -> showFinished()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        hideOverlay()
    }

    private fun showQuestion(question: QuizQuestion, character: Character) {
        binding.progressBar.visibility = View.GONE
        hideOverlay()

        // Обновляем персонажа
        binding.tvCharacterEmoji.text = character.neutralEmoji
        binding.tvCharacterName.text = character.name

        // Обновляем вопрос
        binding.tvQuestion.text = "Как перевести: ${question.questionText}?"

        if (question.questionType == com.brainrot.italiano.domain.model.QuestionType.MULTIPLE_CHOICE) {
            showMultipleChoice(question)
        } else {
            showWrittenInput()
        }
    }

    private fun showMultipleChoice(question: QuizQuestion) {
        binding.layoutOptions.visibility = View.VISIBLE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        hideKeyboard()

        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        question.options.forEachIndexed { index, option ->
            if (index < buttons.size) {
                buttons[index].text = option
                buttons[index].setOnClickListener {
                    viewModel.submitMultipleChoiceAnswer(option)
                }
            }
        }
    }

    private fun showWrittenInput() {
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.etAnswer.text?.clear()

        // Автофокус на поле ввода
        binding.etAnswer.requestFocus()
        showKeyboard()
    }

    private fun setupInputListeners() {
        binding.btnSubmit.setOnClickListener {
            val answer = binding.etAnswer.text.toString()
            if (answer.isNotBlank()) {
                hideKeyboard()
                viewModel.submitAnswer(answer)
            }
        }
    }

    private fun showCorrectOverlay(character: Character) {
        hideKeyboard()

        // Скрываем кнопку "Проверить"
        binding.btnSubmit.visibility = View.GONE

        // Затемняем фон
        overlayBinding.root.visibility = View.VISIBLE
        overlayBinding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_green))
        overlayBinding.root.background.alpha = 220

        // Большой эмодзи
        overlayBinding.tvOverlayEmoji.text = character.happyEmoji

        // Текст
        overlayBinding.tvOverlayText.text = "Правильно! 🎉"
        overlayBinding.tvOverlayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Скрываем правильный ответ (не нужен для правильного)
        overlayBinding.tvOverlayCorrectAnswer.visibility = View.GONE

        // Анимация появления
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        overlayBinding.root.startAnimation(anim)

        // Авто-переход через 2 секунды если не нажали "Далее"
        overlayBinding.root.postDelayed({
            if (isAdded && overlayBinding.root.visibility == View.VISIBLE) {
                hideOverlay()
                viewModel.loadNextQuestion()
            }
        }, 2000)
    }

    private fun showWrongOverlay(character: Character, correctAnswer: String) {
        hideKeyboard()

        // Скрываем кнопку "Проверить"
        binding.btnSubmit.visibility = View.GONE

        // Затемняем фон красным
        overlayBinding.root.visibility = View.VISIBLE
        overlayBinding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_red))
        overlayBinding.root.background.alpha = 220

        // Большой грустный эмодзи
        overlayBinding.tvOverlayEmoji.text = character.sadEmoji

        // Текст
        overlayBinding.tvOverlayText.text = "Неправильно 😢"
        overlayBinding.tvOverlayText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        // Показываем правильный ответ
        overlayBinding.tvOverlayCorrectAnswer.visibility = View.VISIBLE
        overlayBinding.tvOverlayCorrectAnswer.text = "Правильный ответ: $correctAnswer"

        // Анимация shake
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        overlayBinding.tvOverlayEmoji.startAnimation(shake)

        // Авто-переход через 3 секунды
        overlayBinding.root.postDelayed({
            if (isAdded && overlayBinding.root.visibility == View.VISIBLE) {
                hideOverlay()
                viewModel.loadNextQuestion()
            }
        }, 3000)
    }

    private fun hideOverlay() {
        overlayBinding.root.visibility = View.GONE
        overlayBinding.root.removeCallbacks(null)
    }

    private fun showFinished() {
        hideKeyboard()
        hideOverlay()
        binding.tvQuestion.text = "Все слова выучены! 🎉"
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.btnExit.visibility = View.VISIBLE
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etAnswer, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        _overlayBinding = null
        _binding = null
    }
}
