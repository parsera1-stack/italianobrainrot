package com.brainrot.italiano.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentHomeBinding
import com.brainrot.italiano.ui.viewmodel.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeCharacters()
    }

    private fun setupClickListeners() {
        binding.cardLevel1.setOnClickListener {
            navigateToQuiz(1)
        }
        binding.cardLevel2.setOnClickListener {
            navigateToQuiz(2)
        }
        binding.cardLevel3.setOnClickListener {
            navigateToQuiz(3)
        }
        binding.btnLevel4.setOnClickListener {
        findNavController().navigate(R.id.action_homeFragment_to_spellingQuizFragment)
        }
        binding.btnParent.setOnClickListener {
            showPinDialog()
        }
    }

    private fun navigateToQuiz(level: Int) {
        val action = HomeFragmentDirections.actionHomeToQuiz(level)
        findNavController().navigate(action)
    }

    private fun showPinDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "Введите PIN"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Родительский доступ")
            .setView(input)
            .setPositiveButton("Войти") { _, _ ->
                val pin = input.text.toString()
                if (pin == "5005") {
                    findNavController().navigate(R.id.action_home_to_parent)
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Неверный PIN")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeCharacters() {
        // TODO: Setup RecyclerView adapter for characters
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    
}
