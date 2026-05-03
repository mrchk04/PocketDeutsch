package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

enum class GapFillState { PLAYING, CHECKED }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GapFillExerciseUi(
    exercise: VocabExercise.GapFill,
    onNext: () -> Unit
) {
    var screenState by remember { mutableStateOf(GapFillState.PLAYING) }

    // Зберігаємо відповіді користувача: Індекс речення -> Вибране слово
    var userAnswers by remember { mutableStateOf(mapOf<Int, String>()) }

    val wordBank = remember(exercise) {
        exercise.answers.map { answer ->
            answer.replace(Regex("^\\d+\\.\\s*"), "").trim()
        }.shuffled()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = exercise.instruction.ifBlank { "Встав пропущені слова в речення" },
            style = PocketTheme.typography.titleMedium,
            color = PocketTheme.colors.gray500,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // БЛОК 1: РЕЧЕННЯ З ПРОПУСКАМИ
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(exercise.items) { index, sentence ->
                val userAnswer = userAnswers[index]

                // Очищаємо правильну відповідь від цифр для коректного порівняння
                val rawCorrectAnswer = exercise.answers.getOrNull(index) ?: ""
                val correctAnswer = rawCorrectAnswer.replace(Regex("^\\d+\\.\\s*"), "").trim()

                val isChecking = screenState == GapFillState.PLAYING
                val isCorrect = screenState == GapFillState.CHECKED && userAnswer == correctAnswer
                val isWrong = screenState == GapFillState.CHECKED && userAnswer != correctAnswer

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PocketTheme.colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, PocketTheme.colors.gray200, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    // Розбиваємо речення по маркеру пропуску.
                    // ЗАМІНИ "___" на той маркер, який використовується у твоєму JSON (може бути "..." або "[blank]")
                    val parts = sentence.split("___")

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        parts.forEachIndexed { partIndex, part ->
                            // 1. Текст речення
                            Text(
                                text = part,
                                style = PocketTheme.typography.bodyLarge,
                                color = PocketTheme.colors.ink,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )

                            // 2. Блок пропуску (малюємо тільки між частинами тексту)
                            if (partIndex < parts.size - 1) {
                                val boxColor = when {
                                    isCorrect -> PocketTheme.colors.success.copy(alpha = 0.2f)
                                    isWrong -> PocketTheme.colors.error.copy(alpha = 0.2f)
                                    userAnswer != null -> PocketTheme.colors.primary.copy(alpha = 0.1f)
                                    else -> PocketTheme.colors.gray200
                                }
                                val borderColor = when {
                                    isCorrect -> PocketTheme.colors.success
                                    isWrong -> PocketTheme.colors.error
                                    userAnswer != null -> PocketTheme.colors.primary
                                    else -> PocketTheme.colors.gray500
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(boxColor)
                                        .clickable(enabled = screenState == GapFillState.PLAYING && userAnswer != null) {
                                            // Клік по заповненому пропуску прибирає слово (повертає в банк)
                                            val newAnswers = userAnswers.toMutableMap()
                                            newAnswers.remove(index)
                                            userAnswers = newAnswers
                                        }
                                        .border(
                                            width = if (userAnswer != null || screenState == GapFillState.CHECKED) 2.dp else 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userAnswer ?: "      ", // Порожнє місце, якщо немає слова
                                        style = PocketTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                        color = if (isWrong) PocketTheme.colors.error else PocketTheme.colors.ink
                                    )
                                }
                            }
                        }
                    }

                    // Шпаргалка правильної відповіді (якщо помилка)
                    if (isWrong) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Правильна відповідь: $correctAnswer",
                            style = PocketTheme.typography.labelLarge,
                            color = PocketTheme.colors.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // БЛОК 2: БАНК СЛІВ (показуємо тільки ті слова, які ще не використані)
        if (screenState == GapFillState.PLAYING) {
            val availableWords = wordBank.filter { word ->
                // Щоб підтримувати однакові слова в банку (якщо такі є),
                // рахуємо скільки разів слово в банку і скільки разів його використав юзер
                val countInBank = wordBank.count { it == word }
                val countUsed = userAnswers.values.count { it == word }
                countUsed < countInBank
            }.distinct() // Показуємо унікальні варіанти, щоб не дублювати кнопки

            Text(
                text = "Банк слів:",
                style = PocketTheme.typography.labelLarge,
                color = PocketTheme.colors.gray500,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableWords.forEach { word ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PocketTheme.colors.surface)
                            .clickable {
                                // Знаходимо перший порожній пропуск
                                val firstEmptyIndex = exercise.items.indices.firstOrNull { userAnswers[it] == null }
                                if (firstEmptyIndex != null) {
                                    userAnswers = userAnswers + (firstEmptyIndex to word)
                                }
                            }
                            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = word,
                            style = PocketTheme.typography.bodyLarge,
                            color = PocketTheme.colors.ink
                        )
                    }
                }
            }
        }

        // БЛОК 3: КНОПКА
        com.mrchk.pocketdeutsch.ui.components.PdButton(
            text = if (screenState == GapFillState.PLAYING) "Перевірити" else "Далі",
            onClick = {
                if (screenState == GapFillState.PLAYING) {
                    screenState = GapFillState.CHECKED
                } else {
                    onNext()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}