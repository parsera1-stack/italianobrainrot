package com.brainrot.italiano.ui.screens.spelling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentSpellingQuizBinding
import com.brainrot.italiano.ui.viewmodel.SpellingQuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpellingQuizFragment : Fragment() {

    private var _binding: FragmentSpellingQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SpellingQuizViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpellingQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeQuestion()
        observeFeedback()
        observeScore()
        observeLoading()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnNext.setOnClickListener {
            viewModel.loadNextQuestion()
        }
    }

    private fun observeQuestion() {
        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            question?.let {
                binding.tvRussianWord.text = it.russianWord
                binding.tvQuestionNumber.text = "Вопрос ${viewModel.totalQuestions.value ?: 1}"

                setupOptions(it.options, it.correctAnswer)
            }
        }
    }

    private fun setupOptions(options: List<String>, correctAnswer: String) {
        val buttons = listOf(
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3,
            binding.btnOption4
        )

        buttons.forEach { it.visibility = View.GONE }

        options.forEachIndexed { index, option ->
            if (index < buttons.size) {
                val btn = buttons[index]
                btn.text = option
                btn.visibility = View.VISIBLE
                btn.isEnabled = true
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_coffee))

                btn.setOnClickListener {
                    viewModel.checkAnswer(option)
                }
            }
        }
    }

    private fun observeFeedback() {
        viewModel.feedback.observe(viewLifecycleOwner) { feedback ->
            when (feedback) {
                is SpellingQuizViewModel.Feedback.Answer -> {
                    showAnswerResult(feedback.isCorrect, feedback.correctAnswer)
                }
                is SpellingQuizViewModel.Feedback.Error -> {
                    binding.tvRussianWord.text = feedback.message
                }
                null -> {
                    resetOptions()
                }
            }
        }
    }

    private fun showAnswerResult(isCorrect: Boolean, correctAnswer: String) {
        val buttons = listOf(
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3,
            binding.btnOption4
        )

        buttons.forEach { btn ->
            btn.isEnabled = false
            when {
                btn.text.toString() == correctAnswer -> {
                    btn.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.green_correct)
                    )
                }
                !isCorrect && btn.isPressed -> {
                    btn.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.red_wrong)
                    )
                }
            }
        }
    }

    private fun resetOptions() {
        val buttons = listOf(
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3,
            binding.btnOption4
        )

        buttons.forEach { btn ->
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_coffee))
            btn.isEnabled = true
        }
    }

    private fun observeScore() {
        viewModel.score.observe(viewLifecycleOwner) { score ->
            binding.tvScore.text = "Очки: $score"
        }
    }

    private fun observeLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
