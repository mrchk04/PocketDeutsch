package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

enum class WordFormationState { PLAYING, CHECKED }

@Composable
fun WordFormationExerciseUi(
    exercise: VocabExercise.WordFormation,
    onNext: () -> Unit
) {
    var screenState by remember { mutableStateOf(WordFormationState.PLAYING) }
    // Зберігаємо текст, який вводить користувач
    var userInputs by remember { mutableStateOf(mapOf<Int, String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = exercise.instruction.ifBlank { "Напишіть правильну форму слова:" },
            style = PocketTheme.typography.titleMedium,
            color = PocketTheme.colors.gray500,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(exercise.items) { index, task ->
                // Очищаємо правильну відповідь від можливих індексів "1. ", як ми робили раніше
                val rawCorrect = exercise.answers.getOrNull(index) ?: ""
                val correctAnswer = rawCorrect.replace(Regex("^\\d+\\.\\s*"), "").trim()

                val currentInput = userInputs[index] ?: ""
                val isChecking = screenState == WordFormationState.CHECKED

                // Порівнюємо без урахування регістру та зайвих пробілів
                val isCorrect = isChecking && currentInput.trim().equals(correctAnswer, ignoreCase = true)
                val isWrong = isChecking && !currentInput.trim().equals(correctAnswer, ignoreCase = true)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PocketTheme.colors.surface, RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = when {
                                isCorrect -> PocketTheme.colors.success
                                isWrong -> PocketTheme.colors.error
                                else -> PocketTheme.colors.gray200
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = task, // Наприклад: "die Miete (Verb) ->"
                        style = PocketTheme.typography.labelLarge,
                        color = PocketTheme.colors.gray500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { if (!isChecking) userInputs = userInputs + (index to it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Введіть слово...") },
                        enabled = !isChecking,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PocketTheme.colors.primary,
                            unfocusedBorderColor = PocketTheme.colors.gray200,
                            disabledTextColor = PocketTheme.colors.ink,
                            disabledBorderColor = if (isCorrect) PocketTheme.colors.success else if (isWrong) PocketTheme.colors.error else PocketTheme.colors.gray200
                        )
                    )

                    if (isWrong) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Правильно: $correctAnswer",
                            style = PocketTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PocketTheme.colors.error
                        )
                    }
                }
            }
        }

        PdButton(
            text = if (screenState == WordFormationState.PLAYING) "Перевірити" else "Далі",
            onClick = {
                if (screenState == WordFormationState.PLAYING) screenState = WordFormationState.CHECKED
                else onNext()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}