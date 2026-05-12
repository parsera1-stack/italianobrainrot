package com.brainrot.italiano.ui.screens.parent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.brainrot.italiano.databinding.FragmentParentBinding
import com.brainrot.italiano.ui.viewmodel.ParentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParentFragment : Fragment() {

    private var _binding: FragmentParentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ParentViewModel by viewModels()

    private val csvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.importFromCsv(requireContext(), uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupClickListeners()
        observeWords()
        observeImportResult()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Слова"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Статистика"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showWordsTab()
                    1 -> showStatsTab()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnImportCsv.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
            }
            csvLauncher.launch(intent)
        }

        binding.btnAddWord.setOnClickListener {
            showAddWordDialog()
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showAddWordDialog() {
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 30, 50, 0)
        }

        val etRussian = android.widget.EditText(requireContext()).apply {
            hint = "Русский"
        }
        val etEnglish = android.widget.EditText(requireContext()).apply {
            hint = "English"
        }

        layout.addView(etRussian)
        layout.addView(etEnglish)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавить слово")
            .setView(layout)
            .setPositiveButton("Добавить") { _, _ ->
                val russian = etRussian.text.toString().trim()
                val english = etEnglish.text.toString().trim()
                if (russian.isNotBlank() && english.isNotBlank()) {
                    viewModel.addWord(russian, english)
                    Snackbar.make(binding.root, "Слово добавлено", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeWords() {
        viewModel.words.observe(viewLifecycleOwner) { words ->
            // TODO: Update RecyclerView adapter
        }
    }

    private fun observeImportResult() {
        viewModel.importResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                it.onSuccess { count ->
                    Snackbar.make(binding.root, "Загружено $count слов", Snackbar.LENGTH_LONG).show()
                }.onFailure { error ->
                    Snackbar.make(binding.root, "Ошибка: ${error.message}", Snackbar.LENGTH_LONG).show()
                }
                viewModel.clearImportResult()
            }
        }
    }

    private fun showWordsTab() {
        // TODO: Show words RecyclerView
    }

    private fun showStatsTab() {
        // TODO: Show statistics view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
