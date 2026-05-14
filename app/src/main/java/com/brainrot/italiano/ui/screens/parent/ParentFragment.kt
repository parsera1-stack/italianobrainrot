package com.brainrot.italiano.ui.screens.parent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brainrot.italiano.R
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

    private lateinit var wordsAdapter: WordsAdapter

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
        setupRecyclerView()
        setupClickListeners()
        observeWords()
        observeImportResult()
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Слова (${viewModel.words.value?.size ?: 0})"))
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

    private fun setupRecyclerView() {
        wordsAdapter = WordsAdapter(
            onDelete = { word ->
                viewModel.deleteWord(word)
                Snackbar.make(binding.root, "Слово удалено", Snackbar.LENGTH_SHORT).show()
            },
            onToggleLearned = { word ->
                viewModel.toggleLearned(word)
            }
        )
        binding.rvWords.adapter = wordsAdapter
        binding.rvWords.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        binding.btnImportCsv.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values", "text/plain"))
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
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 0)
        }

        val etRussian = android.widget.EditText(requireContext()).apply {
            hint = "Русский (например: собака)"
        }
        val etEnglish = android.widget.EditText(requireContext()).apply {
            hint = "English (например: dog)"
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
            wordsAdapter.submitList(words)
            // Обновляем заголовок вкладки
            binding.tabLayout.getTabAt(0)?.text = "Слова (${words.size})"
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
        binding.rvWords.visibility = View.VISIBLE
        // Скрываем статистику если есть
        val statsView = binding.root.findViewWithTag<View>("stats_view")
        statsView?.visibility = View.GONE
    }

    private fun showStatsTab() {
        binding.rvWords.visibility = View.GONE

        // Показываем статистику
        val words = viewModel.words.value ?: emptyList()
        val statsView = binding.root.findViewWithTag<View>("stats_view")

        if (statsView == null) {
            val newStatsView = createStatsView(words)
            newStatsView.tag = "stats_view"
            (binding.root as ViewGroup).addView(newStatsView)
        } else {
            statsView.visibility = View.VISIBLE
            updateStatsView(statsView, words)
        }
    }

    private fun createStatsView(words: List<com.brainrot.italiano.domain.model.Word>): View {
        val scrollView = android.widget.ScrollView(requireContext())
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        updateStatsView(layout, words)
        scrollView.addView(layout)
        return scrollView
    }

    private fun updateStatsView(view: View, words: List<com.brainrot.italiano.domain.model.Word>) {
        val layout = view as? LinearLayout ?: (view as android.widget.ScrollView).getChildAt(0) as LinearLayout
        layout.removeAllViews()

        // Заголовок
        val title = TextView(requireContext()).apply {
            text = "📊 Статистика"
            textSize = 24f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)

        // Общая статистика
        val totalWords = words.size
        val learnedWords = words.count { it.isLearned }
        val activeWords = totalWords - learnedWords

        addStatLine(layout, "Всего слов:", "$totalWords")
        addStatLine(layout, "Выучено:", "$learnedWords")
        addStatLine(layout, "В процессе:", "$activeWords")
        addStatLine(layout, "Показано вопросов:", "${words.sumOf { it.totalShows }}")
        addStatLine(layout, "Правильных ответов:", "${words.sumOf { it.totalCorrect }}")
        addStatLine(layout, "Неправильных ответов:", "${words.sumOf { it.totalWrong }}")

        // Детальная статистика по словам
        val detailTitle = TextView(requireContext()).apply {
            text = "\n📋 По словам:"
            textSize = 20f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
            setPadding(0, 24, 0, 16)
        }
        layout.addView(detailTitle)

        words.sortedByDescending { it.totalShows }.forEach { word ->
            val accuracy = if (word.totalShows > 0) {
                "${(word.totalCorrect * 100 / word.totalShows)}%"
            } else "0%"

            addStatLine(layout, 
                "${word.russian} = ${word.english}", 
                "Показано: ${word.totalShows} | Правильно: ${word.totalCorrect} | Точность: $accuracy",
                isSmall = true
            )
        }
    }

    private fun addStatLine(layout: LinearLayout, label: String, value: String, isSmall: Boolean = false) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 8)
        }

        val labelView = TextView(requireContext()).apply {
            text = label
            textSize = if (isSmall) 14f else 16f
            setTextColor(resources.getColor(R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val valueView = TextView(requireContext()).apply {
            text = value
            textSize = if (isSmall) 12f else 14f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
        }

        row.addView(labelView)
        row.addView(valueView)
        layout.addView(row)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
