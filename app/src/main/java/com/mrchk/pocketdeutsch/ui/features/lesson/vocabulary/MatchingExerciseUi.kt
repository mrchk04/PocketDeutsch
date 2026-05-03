package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

enum class MatchingState { PLAYING, CHECKED }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MatchingExerciseUi(
    exercise: VocabExercise.Matching,
    onNext: () -> Unit
) {
    var screenState by remember { mutableStateOf(MatchingState.PLAYING) }
    var userMatches by remember { mutableStateOf(mapOf<String, String>()) }

    var selectedWord by remember { mutableStateOf<String?>(null) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // 💡 Створюємо "шпаргалку" правильних відповідей одразу при завантаженні вправи
    val correctAnswersMap = remember(exercise) {
        exercise.options.associateWith { option ->
            val optionPrefix = option.substringBefore(")").trim() // "a"
            val answerPair = exercise.answers.find { it.endsWith("-$optionPrefix") } // "1-a"
            val wordPrefix = answerPair?.substringBefore("-")?.trim() // "1"
            // Знаходимо повне слово по його префіксу
            exercise.items.find { it.substringBefore(".").trim() == wordPrefix } ?: ""
        }
    }

    fun makeMatch(word: String, option: String) {
        userMatches = userMatches + (word to option)
        selectedWord = null
        selectedOption = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = exercise.instruction.ifBlank { "З'єднай слова з їх значеннями" },
            style = PocketTheme.typography.titleMedium,
            color = PocketTheme.colors.gray500,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // БЛОК 1: СЛОВА (ТЕГИ)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exercise.items.forEach { word ->
                val isMatchedByUser = userMatches.containsKey(word)
                val isSelected = selectedWord == word
                val isChecking = screenState == MatchingState.CHECKED

                val isCorrectlyGuessed = if (isChecking && isMatchedByUser) {
                    val userOption = userMatches[word]!!
                    correctAnswersMap[userOption] == word
                } else false

                val isWrong = isChecking && (!isCorrectlyGuessed)

                val bgColor = when {
                    isCorrectlyGuessed -> PocketTheme.colors.success.copy(alpha = 0.2f)
                    isWrong -> PocketTheme.colors.error.copy(alpha = 0.2f)
                    isSelected -> PocketTheme.colors.primary.copy(alpha = 0.2f)
                    isMatchedByUser && !isChecking -> PocketTheme.colors.gray200
                    else -> PocketTheme.colors.surface
                }

                val borderColor = when {
                    isCorrectlyGuessed -> PocketTheme.colors.success
                    isWrong -> PocketTheme.colors.error
                    isSelected -> PocketTheme.colors.primary
                    else -> PocketTheme.colors.ink
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable(enabled = screenState == MatchingState.PLAYING) {
                            if (isSelected) {
                                selectedWord = null
                            } else {
                                selectedWord = word
                                if (selectedOption != null) {
                                    makeMatch(word, selectedOption!!)
                                }
                            }
                        }
                        .border(
                            width = if (isSelected || isChecking) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = word,
                        style = PocketTheme.typography.labelLarge,
                        color = if (isWrong) PocketTheme.colors.error else PocketTheme.colors.ink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PocketTheme.colors.gray200))
        Spacer(modifier = Modifier.height(16.dp))

        // БЛОК 2: ВИЗНАЧЕННЯ (СПИСОК)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercise.options) { option ->
                val matchedWordForThisOption = userMatches.entries.find { it.value == option }?.key
                val isMatchedByUser = matchedWordForThisOption != null
                val isSelected = selectedOption == option
                val isChecking = screenState == MatchingState.CHECKED

                val isCorrectlyGuessed = if (isChecking && isMatchedByUser) {
                    correctAnswersMap[option] == matchedWordForThisOption
                } else false

                val isWrong = isChecking && !isCorrectlyGuessed

                val bgColor = when {
                    isCorrectlyGuessed -> PocketTheme.colors.success.copy(alpha = 0.2f)
                    isWrong -> PocketTheme.colors.error.copy(alpha = 0.1f) // Робимо фон червоного м'якішим, щоб текст легко читався
                    isSelected -> PocketTheme.colors.primary.copy(alpha = 0.2f)
                    isMatchedByUser && !isChecking -> PocketTheme.colors.gray200
                    else -> PocketTheme.colors.surface
                }

                val borderColor = when {
                    isCorrectlyGuessed -> PocketTheme.colors.success
                    isWrong -> PocketTheme.colors.error
                    isSelected -> PocketTheme.colors.primary
                    else -> PocketTheme.colors.ink
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable(enabled = screenState == MatchingState.PLAYING) {
                            if (isSelected) {
                                selectedOption = null
                            } else {
                                selectedOption = option
                                if (selectedWord != null) {
                                    makeMatch(selectedWord!!, option)
                                }
                            }
                        }
                        .border(
                            width = if (isSelected || isChecking) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = option,
                            style = PocketTheme.typography.bodyMedium,
                            color = PocketTheme.colors.ink
                        )

                        if (isWrong) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Правильна відповідь: ${correctAnswersMap[option]}",
                                style = PocketTheme.typography.labelLarge,
                                color = PocketTheme.colors.error // Або PocketTheme.colors.ink, залежить від того, як краще читається
                            )
                        }
                    }
                }
            }
        }

        com.mrchk.pocketdeutsch.ui.components.PdButton(
            text = if (screenState == MatchingState.PLAYING) "Перевірити" else "Далі",
            onClick = {
                if (screenState == MatchingState.PLAYING) {
                    screenState = MatchingState.CHECKED
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
        )
    }
}