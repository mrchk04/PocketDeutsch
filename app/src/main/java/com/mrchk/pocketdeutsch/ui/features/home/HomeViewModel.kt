package com.mrchk.pocketdeutsch.ui.features.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Моделі для UI
data class WordOfDayState(
    val word: String = "",
    val partOfSpeech: String = "",
    val translation: String = "",
    val example: String? = null
)

data class CourseProgressState(
    val level: String = "",
    val title: String = "",
    val progress: Float = 0f
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _userName = MutableStateFlow("Pocket Deutsch!")
    val userName = _userName.asStateFlow()

    // Заглушка для Слова дня (згодом можна тягнути з БД/API)
    private val _wordOfDay = MutableStateFlow(
        WordOfDayState(
            word = "Schlafen",
            partOfSpeech = "Verb",
            translation = "Спати",
            example = "Ich schlafe mit vielen Kissen"
        )
    )
    val wordOfDay = _wordOfDay.asStateFlow()

    // Заглушка для прогресу
    private val _courseProgress = MutableStateFlow(
        CourseProgressState(
            level = "B1.2",
            title = "Unterricht 2 | Urlaub",
            progress = 0.55f
        )
    )
    val courseProgress = _courseProgress.asStateFlow()
}