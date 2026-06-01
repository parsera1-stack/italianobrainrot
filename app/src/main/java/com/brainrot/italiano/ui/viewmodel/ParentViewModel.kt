package com.brainrot.italiano.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainrot.italiano.domain.model.Word
import com.brainrot.italiano.domain.usecase.UnifiedImportExportUseCase
import com.brainrot.italiano.data.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val repository: WordRepository,
    private val unifiedImportExport: UnifiedImportExportUseCase
) : ViewModel() {

    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words

    private val _operationResult = MutableLiveData<String?>()
    val operationResult: LiveData<String?> = _operationResult

    private val _pinValidated = MutableLiveData<Boolean>()
    val pinValidated: LiveData<Boolean> = _pinValidated

    init {
        loadWords()
    }

    private fun loadWords() {
        viewModelScope.launch {
            repository.getAllWords().collectLatest { list ->
                _words.value = list
            }
        }
    }

    fun validatePin(pin: String) {
        _pinValidated.value = pin == "5005"
    }

    fun addWord(russian: String, english: String) {
        viewModelScope.launch {
            val word = Word(russian = russian, english = english)
            repository.addWord(word)
        }
    }

    fun updateWord(word: Word) {
        viewModelScope.launch {
            repository.updateWord(word)
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    fun toggleLearned(word: Word) {
        viewModelScope.launch {
            repository.updateWord(word.copy(isLearned = !word.isLearned))
        }
    }

    /**
     * Экспорт слов и статистики в CSV
     */
    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = unifiedImportExport.exportToCsv(context, uri)
            result.onSuccess { count ->
                _operationResult.value = "Экспортировано $count слов"
            }.onFailure { error ->
                _operationResult.value = "Ошибка экспорта: ${error.message}"
            }
        }
    }

    /**
     * Импорт слов и статистики из CSV
     */
    fun importFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = unifiedImportExport.importFromCsv(context, uri)
            result.onSuccess { importResult ->
                _operationResult.value = "Импорт: $importResult"
            }.onFailure { error ->
                _operationResult.value = "Ошибка импорта: ${error.message}"
            }
        }
    }

    fun clearOperationResult() {
        _operationResult.value = null
    }
}
