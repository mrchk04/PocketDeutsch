package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mrchk.pocketdeutsch.domain.model.VocabExercise
import com.mrchk.pocketdeutsch.ui.components.BrutalistCard
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import kotlinx.coroutines.delay

@Composable
fun VocabularyPracticeScreen(
    viewModel: VocabularyPracticeViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit // Викликається, коли всі вправи пройдені
) {

    var timeSpentSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            timeSpentSeconds++
        }
    }

    val exercises by viewModel.exercises.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val progress = viewModel.getProgress()

    var showCompletionDialog by remember { mutableStateOf(false) }
    var restartKey by remember { mutableIntStateOf(0) }

    if (exercises.isEmpty()) {
        return
    }

    val currentExercise = exercises[currentIndex]

    Scaffold(
        topBar = {
            PdExerciseTopBar(
                progress = progress,
                progressText = "${currentIndex + 1} / ${exercises.size}",
                onBackClick = onBackClick
            )
        },
        containerColor = PocketTheme.colors.paper
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // 1. СТВОРЮЄМО УНІВЕРСАЛЬНИЙ ОБРОБНИК КРОКУ
            val handleNextStep = { exerciseType: String ->
                // Зберігаємо звіт
                com.mrchk.pocketdeutsch.utils.TestAnalytics.addReport(
                    com.mrchk.pocketdeutsch.utils.ExerciseReport(
                        exerciseName = "Лексика Практика ($exerciseType, Вправа ${currentIndex + 1})",
                        timeSpentSeconds = timeSpentSeconds,
                        correctAnswers = 0, // Бали тут ставимо 0 (або якщо зможеш витягнути зсередини UI - передавай сюди)
                        totalQuestions = 0
                    )
                )

                // Обнуляємо таймер
                timeSpentSeconds = 0

                // Йдемо далі або завершуємо
                if (currentIndex == exercises.size - 1) {
                    showCompletionDialog = true
                } else {
                    viewModel.nextExercise()
                }
            }
            key(currentIndex, restartKey) {
                when (currentExercise) {
                    is VocabExercise.Matching -> {
                        MatchingExerciseUi(
                            exercise = currentExercise,
                            onNext = {
                                if (currentIndex == exercises.size - 1) {
                                    showCompletionDialog = true
                                } else {
                                    viewModel.nextExercise()
                                }
                            }
                        )
                    }

                    is VocabExercise.GapFill -> {
                        GapFillExerciseUi(
                            exercise = currentExercise,
                            onNext = {
                                if (currentIndex == exercises.size - 1) {
                                    showCompletionDialog = true
                                } else {
                                    viewModel.nextExercise()
                                }
                            }
                        )
                    }

                    is VocabExercise.WordFormation -> {
                        WordFormationExerciseUi(
                            exercise = currentExercise,
                            onNext = {
                                if (currentIndex == exercises.size - 1) {
                                    showCompletionDialog = true
                                } else {
                                    viewModel.nextExercise()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCompletionDialog) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PocketTheme.colors.surface)
                    .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Практику завершено!",
                        style = PocketTheme.typography.headlineSmall,
                        color = PocketTheme.colors.ink,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Успішно виконано всі завдання. Результат збережено.",
                        style = PocketTheme.typography.bodyMedium,
                        color = PocketTheme.colors.ink,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    com.mrchk.pocketdeutsch.ui.components.PdButton(
                        text = "Повернутися до слів",
                        onClick = {
                            showCompletionDialog = false
                            onComplete()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    com.mrchk.pocketdeutsch.ui.components.PdButton(
                        text = "Пройти ще раз",
                        onClick = {
                            showCompletionDialog = false
                            viewModel.resetPractice() // Скидаємо індекс на 0
                            restartKey++ // Примусово оновлюємо UI, щоб стерти старі відповіді
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}