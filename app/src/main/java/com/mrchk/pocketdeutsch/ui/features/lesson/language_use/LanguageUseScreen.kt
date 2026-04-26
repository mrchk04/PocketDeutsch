package com.mrchk.pocketdeutsch.ui.features.lesson.language_use

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrchk.pocketdeutsch.domain.model.LanguageUsePractice
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.components.pdClickable
import com.mrchk.pocketdeutsch.ui.components.pdStyle
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LanguageUseScreen(
    viewModel: LanguageUseViewModel = viewModel(),
    onComplete: () -> Unit,
    onBackClick: () -> Unit,
) {
    val allExercises by viewModel.allExercises.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val activeGap by viewModel.activeGap.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()

    val exercisesList = allExercises

    if (exercisesList == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    if (exercisesList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Вправ для цього модуля не знайдено", style = PocketTheme.typography.titleMedium)
        }
        return
    }

    val currentExercise = exercisesList[currentIndex]

    if (currentExercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator(
                color = PocketTheme.colors.primary
            )
        }
        return
    }

    Scaffold(
        topBar = {
            val totalSteps = exercisesList.size
            val currentStepProgress = currentIndex + 1
            val totalGapsInAll = exercisesList.sumOf { it.gaps.size }
            val completedGapsBefore = exercisesList.take(currentIndex).sumOf { it.gaps.size }
            val currentFilled = selectedAnswers.size
            val overallProgress =
                (completedGapsBefore + currentFilled).toFloat() / totalGapsInAll.toFloat()

            PdExerciseTopBar(
                progress = overallProgress,
                progressText = "$currentStepProgress/$totalSteps", // Показуємо 1/2 вправ
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            // Кнопка перевірки внизу
            Box(modifier = Modifier.padding(20.dp)) {
                PdButton(
                    text = when {
                        !isChecked -> "Перевірити"
                        currentIndex < exercisesList.size - 1 -> "Наступна вправа"
                        else -> "Завершити"
                    },
                    onClick = {
                        if (!isChecked) {
                            viewModel.checkAnswers()
                        } else {
                            viewModel.moveToNextStep(onAllFinished = onComplete)
                        }
                    },
                    backgroundColor = if (isChecked) PocketTheme.colors.success else PocketTheme.colors.ink,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Заголовок та інструкція
            Text(
                text = if (currentExercise.subtype == "lexical_context") "Лексика" else "Граматика",
                style = PocketTheme.typography.labelSmall,
                color = PocketTheme.colors.gray500
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentExercise.instruction,
                style = PocketTheme.typography.headlineSmall,
                color = PocketTheme.colors.ink
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Картка з текстом
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pdStyle(
                        backgroundColor = PocketTheme.colors.surface,
                        cornerRadius = 24.dp,
                        borderWidth = 2.dp
                    )
                    .padding(20.dp)
            ) {
                /*InteractiveGapText(
                    textWithGaps = exercise.textWithGaps,
                    selectedAnswers = selectedAnswers,
                    isChecked = isChecked,
                    exerciseItems = exercise.gaps,
                    onGapClick = { num ->
                        val item = exercise.gaps.find { it.gapNumber == num }
                        item?.let { viewModel.onGapClick(it) }
                    }
                )*/
                InteractiveGapText(
                    textWithGaps = currentExercise.textWithGaps,
                    selectedAnswers = selectedAnswers,
                    isChecked = isChecked,
                    exerciseItems = currentExercise.gaps,
                    activeGapNumber = activeGap?.gapNumber, // Передаємо поточний відкритий пропуск
                    onGapClick = { num ->
                        val item = currentExercise.gaps.find { it.gapNumber == num }
                        item?.let { viewModel.onGapClick(it) }
                    },
                    onOptionSelect = { num, option ->
                        viewModel.selectOption(num, option)
                    },
                    onDismissMenu = {
                        // Якщо функція закриття не реалізована у ViewModel, просто передай 0 та ""
                        viewModel.selectOption(0, "")
                    }
                )
            }
            if (isChecked) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Аналіз відповідей",
                    style = PocketTheme.typography.titleLarge,
                    color = PocketTheme.colors.ink
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Проходимося по всіх пропусках поточної вправи
                currentExercise.gaps.forEach { gap ->
                    val userAnswer = selectedAnswers[gap.gapNumber]
                    val isCorrect = userAnswer == gap.correctAnswer

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            // 1. Стандартний фон (тепер можна безпечно використовувати альфа-канал)
                            .background(
                                color = if (isCorrect) PocketTheme.colors.success.copy(alpha = 0.1f) else PocketTheme.colors.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            // 2. Стандартна рамка
                            .border(
                                width = 2.dp,
                                color = if (isCorrect) PocketTheme.colors.success else PocketTheme.colors.error,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Номер пропуску (залишаємо яскравим для контрасту)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = if (isCorrect) PocketTheme.colors.success else PocketTheme.colors.error,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = gap.gapNumber.toString(),
                                        color = Color.White,
                                        style = PocketTheme.typography.labelSmall
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = if (isCorrect) {
                                        "Правильно: ${gap.correctAnswer}"
                                    } else {
                                        "Ваша: ${userAnswer ?: "—"}  •  Правильно: ${gap.correctAnswer}"
                                    },
                                    style = PocketTheme.typography.titleSmall,
                                    color = PocketTheme.colors.ink
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = gap.explanation,
                                style = PocketTheme.typography.bodyMedium,
                                color = PocketTheme.colors.ink
                            )
                        }
                    }
                }

                // Додатковий відступ знизу, щоб кнопка не перекривала останню картку
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}