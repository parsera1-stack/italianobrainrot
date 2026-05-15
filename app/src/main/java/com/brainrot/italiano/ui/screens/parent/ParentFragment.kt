package com.brainrot.italiano.ui.screens.parent

import android.app.Activity
import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ParentFragment : Fragment() {

    private var _binding: FragmentParentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ParentViewModel by viewModels()

    private lateinit var wordsAdapter: WordsAdapter
    private var selectedDate = Date()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
        binding.statsContainer.visibility = View.GONE
    }

    private fun showStatsTab() {
        binding.rvWords.visibility = View.GONE
        binding.statsContainer.visibility = View.VISIBLE
        updateStatsView()
    }

    private fun updateStatsView() {
        val words = viewModel.words.value ?: emptyList()
        val container = binding.statsContainer
        container.removeAllViews()
        container.orientation = LinearLayout.VERTICAL

        // Заголовок
        val title = TextView(requireContext()).apply {
            text = "📊 Статистика"
            textSize = 28f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
            setPadding(0, 16, 0, 24)
        }
        container.addView(title)

        // Выбор даты
        val dateRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
        }

        val tvDate = TextView(requireContext()).apply {
            text = "📅 Период: ${dateFormat.format(selectedDate)}"
            textSize = 16f
            setTextColor(resources.getColor(R.color.black, null))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val btnPickDate = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = "Выбрать дату"
            textSize = 12f
            setPadding(16, 8, 16, 8)
            setOnClickListener {
                showDatePicker(tvDate)
            }
        }

        dateRow.addView(tvDate)
        dateRow.addView(btnPickDate)
        container.addView(dateRow)

        // Последние 3 дня по умолчанию
        val last3Days = TextView(requireContext()).apply {
            text = "(по умолчанию: последние 3 дня)"
            textSize = 12f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
            setPadding(0, 0, 0, 24)
        }
        container.addView(last3Days)

        // Общая статистика — таблица
        val totalWords = words.size
        val learnedWords = words.count { it.isLearned }
        val activeWords = totalWords - learnedWords
        val totalShows = words.sumOf { it.totalShows }
        val totalCorrect = words.sumOf { it.totalCorrect }
        val totalWrong = words.sumOf { it.totalWrong }
        val accuracy = if (totalShows > 0) "${(totalCorrect * 100 / totalShows)}%" else "0%"

        addStatsTable(container, listOf(
            Triple("Всего слов", "$totalWords", ""),
            Triple("Выучено", "$learnedWords", "✅"),
            Triple("В процессе", "$activeWords", "📚"),
            Triple("Показано", "$totalShows", "👁️"),
            Triple("Правильно", "$totalCorrect", "🎯"),
            Triple("Неправильно", "$totalWrong", "❌"),
            Triple("Точность", accuracy, "📈")
        ))

        // Детальная статистика по словам — таблица
        val detailTitle = TextView(requireContext()).apply {
            text = "
📋 По словам"
            textSize = 22f
            setTextColor(resources.getColor(R.color.coffee_dark, null))
            setPadding(0, 32, 0, 16)
        }
        container.addView(detailTitle)

        // Заголовок таблицы
        addTableHeader(container, listOf("Слово", "Показано", "Правильно", "Точность"))

        words.sortedByDescending { it.totalShows }.forEach { word ->
            val wordAccuracy = if (word.totalShows > 0) {
                "${(word.totalCorrect * 100 / word.totalShows)}%"
            } else "0%"

            addTableRow(container, listOf(
                "${word.russian}
${word.english}",
                "${word.totalShows}",
                "${word.totalCorrect}",
                wordAccuracy
            ))
        }
    }

    private fun showDatePicker(tvDate: TextView) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = calendar.time
                tvDate.text = "📅 Период: ${dateFormat.format(selectedDate)}"
                updateStatsView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addStatsTable(container: LinearLayout, rows: List<Triple<String, String, String>>) {
        val tableLayout = android.widget.TableLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        rows.forEach { (label, value, icon) ->
            val row = android.widget.TableRow(requireContext()).apply {
                setPadding(8, 12, 8, 12)
                setBackgroundColor(resources.getColor(R.color.white, null))
            }

            val labelView = TextView(requireContext()).apply {
                text = "$icon $label"
                textSize = 14f
                setTextColor(resources.getColor(R.color.black, null))
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 2f)
            }

            val valueView = TextView(requireContext()).apply {
                text = value
                textSize = 16f
                setTextColor(resources.getColor(R.color.coffee_dark, null))
                textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }

            row.addView(labelView)
            row.addView(valueView)
            tableLayout.addView(row)

            // Разделитель
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(resources.getColor(R.color.pastel_coffee, null))
            }
            tableLayout.addView(divider)
        }

        container.addView(tableLayout)
    }

    private fun addTableHeader(container: LinearLayout, headers: List<String>) {
        val row = android.widget.TableRow(requireContext()).apply {
            setPadding(8, 12, 8, 12)
            setBackgroundColor(resources.getColor(R.color.pastel_coffee, null))
        }

        headers.forEach { header ->
            val tv = TextView(requireContext()).apply {
                text = header
                textSize = 12f
                setTextColor(resources.getColor(R.color.white, null))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(4, 4, 4, 4)
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(tv)
        }

        val tableLayout = android.widget.TableLayout(requireContext())
        tableLayout.addView(row)
        container.addView(tableLayout)
    }

    private fun addTableRow(container: LinearLayout, cells: List<String>) {
        val tableLayout = container.getChildAt(container.childCount - 1) as android.widget.TableLayout

        val row = android.widget.TableRow(requireContext()).apply {
            setPadding(8, 8, 8, 8)
            setBackgroundColor(resources.getColor(R.color.white, null))
        }

        cells.forEach { cell ->
            val tv = TextView(requireContext()).apply {
                text = cell
                textSize = 11f
                setTextColor(resources.getColor(R.color.black, null))
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(4, 4, 4, 4)
                layoutParams = android.widget.TableRow.LayoutParams(0, android.widget.TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(tv)
        }

        tableLayout.addView(row)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
