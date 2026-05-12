package com.brainrot.italiano.ui.screens.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentQuizBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startQuiz(args.level)
        observeQuizState()
        setupInputListeners()
    }

    private fun observeQuizState() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizState.Loading -> showLoading()
                is QuizState.Question -> showQuestion(state.question, state.character)
                is QuizState.Correct -> showCorrect(state.character)
                is QuizState.Wrong -> showWrong(state.character, state.correctAnswer)
                is QuizState.Finished -> showFinished()
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.cardFeedback.visibility = View.GONE
    }

    private fun showQuestion(question: QuizQuestion, character: Character) {
        binding.progressBar.visibility = View.GONE
        binding.cardFeedback.visibility = View.GONE

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
    }

    private fun setupInputListeners() {
        binding.btnSubmit.setOnClickListener {
            val answer = binding.etAnswer.text.toString()
            if (answer.isNotBlank()) {
                viewModel.submitAnswer(answer)
            }
        }
    }

    private fun showCorrect(character: Character) {
        binding.tvCharacterEmoji.text = character.happyEmoji
        binding.cardFeedback.apply {
            visibility = View.VISIBLE
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_green))
        }
        binding.tvFeedback.apply {
            text = "Правильно! 🎉"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.coffee_dark))
        }

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.cardFeedback.startAnimation(anim)
    }

    private fun showWrong(character: Character, correctAnswer: String) {
        binding.tvCharacterEmoji.text = character.sadEmoji
        binding.cardFeedback.apply {
            visibility = View.VISIBLE
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_red))
        }
        binding.tvFeedback.apply {
            text = "Неправильно! Правильный ответ: $correctAnswer"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        binding.cardQuestion.startAnimation(anim)
    }

    private fun showFinished() {
        binding.tvQuestion.text = "Все слова выучены! 🎉"
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
