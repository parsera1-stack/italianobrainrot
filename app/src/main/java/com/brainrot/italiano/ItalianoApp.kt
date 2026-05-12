package com.brainrot.italiano

import android.app.Application
import com.brainrot.italiano.data.repository.WordRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ItalianoApp : Application() {

    @Inject
    lateinit var wordRepository: WordRepository

    override fun onCreate() {
        super.onCreate()
        // Инициализация стартового словаря при первом запуске
        CoroutineScope(Dispatchers.IO).launch {
            wordRepository.initializeWordsFromDefault()
        }
    }
}
