package com.mrchk.pocketdeutsch.ui.features.lesson.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.components.pdStyle
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun GrammarPracticeScreen(
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
    viewModel: GrammarPracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentExercise = uiState.currentExercise

    Scaffold(
        containerColor = PocketTheme.colors.paper,
        topBar = {
            PdTitleTopBar(
                title = "Практика",
                onBackClick = onBackClick,
                rightButtonIcon = null,
                onRightButtonClick = {}
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PocketTheme.colors.paper)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(PocketTheme.colors.ink)
                )
                Box(modifier = Modifier.padding(16.dp).padding(bottom = 8.dp)) {
                    PdButton(
                        text = if (uiState.isChecked) "Далі" else "Перевірити",
                        onClick = {
                            if (uiState.isChecked) viewModel.nextExercise() else viewModel.checkAnswers()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PocketTheme.colors.primary)
            }
        } else {
            currentExercise?.let { exercise ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Прогрес
                    Text(
                        text = "${uiState.currentIndex + 1} з ${uiState.exercises.size}",
                        style = PocketTheme.typography.labelLarge,
                        color = PocketTheme.colors.ink.copy(alpha = 0.5f)
                    )

                    Text(
                        text = exercise.instruction,
                        style = PocketTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = PocketTheme.colors.ink
                    )

                    when (exercise.type) {
                        "gap_fill", "error_correction", "sentence_construction" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                exercise.items.forEachIndexed { index, itemText ->
                                    ExerciseItemRow(
                                        itemText = itemText,
                                        userAnswer = uiState.userAnswers[index] ?: "",
                                        correctAnswer = exercise.answers.getOrNull(index) ?: "",
                                        isCorrect = uiState.evaluationResults[index],
                                        isChecked = uiState.isChecked,
                                        onAnswerChange = { viewModel.updateAnswer(index, it) }
                                    )
                                }
                            }
                        }

                        "free_production" -> {
                            FreeProductionExerciseContent(
                                userAnswer = uiState.userAnswers[0] ?: "",
                                onAnswerChange = { viewModel.updateAnswer(0, it) },
                                isChecked = uiState.isChecked,
                                isEvaluating = uiState.isEvaluatingAi,
                                isCorrect = uiState.evaluationResults[0],
                                aiFeedback = uiState.aiFeedbackText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (uiState.isFinished) {
        FinishPracticeDialog(
            onRestartClick = { viewModel.resetProgress() },
            onFinishClick = {
                onComplete()
            }
        )
    }
}

@Composable
fun ExerciseItemRow(
    itemText: String,
    userAnswer: String,
    correctAnswer: String,
    isCorrect: Boolean?,
    isChecked: Boolean,
    onAnswerChange: (String) -> Unit
) {
    val borderColor = when {
        !isChecked -> PocketTheme.colors.ink
        isCorrect == true -> PocketTheme.colors.success
        else -> PocketTheme.colors.error
    }

    val backgroundColor = when {
        !isChecked -> Color.White
        isCorrect == true -> PocketTheme.colors.success.copy(alpha = 0.1f)
        else -> Color.Red.copy(alpha = 0.1f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = itemText,
            style = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = PocketTheme.colors.ink
        )

        TextField(
            value = userAnswer,
            onValueChange = onAnswerChange,
            enabled = !isChecked,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            placeholder = { Text("Впишіть відповідь...") },
            shape = RoundedCornerShape(12.dp)
        )

        if (isChecked && isCorrect == false && correctAnswer.isNotEmpty()) {
            val cleanCorrectAnswer = correctAnswer.replaceFirst(Regex("^\\d+\\.\\s*"), "")
            Text(
                text = "Правильна відповідь: $cleanCorrectAnswer",
                style = PocketTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Red
            )
        }
    }
}

@Composable
fun FreeProductionExerciseContent(
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    isChecked: Boolean,
    isEvaluating: Boolean,
    isCorrect: Boolean?,
    aiFeedback: String?
) {
    val borderColor = when {
        !isChecked || isEvaluating -> PocketTheme.colors.ink
        isCorrect == true -> PocketTheme.colors.success
        else -> Color.Red
    }

    val backgroundColor = when {
        !isChecked || isEvaluating -> Color.White
        isCorrect == true -> PocketTheme.colors.success.copy(alpha = 0.1f)
        else -> Color.Red.copy(alpha = 0.1f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        TextField(
            value = userAnswer,
            onValueChange = onAnswerChange,
            enabled = !isChecked || isEvaluating,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)), // <--- ЗАСТОСУВАЛИ
            colors = TextFieldDefaults.colors(
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = if (isEvaluating) PocketTheme.colors.gray200 else backgroundColor, // <--- ЗАСТОСУВАЛИ
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = PocketTheme.typography.bodyLarge,
            placeholder = { Text("Напишіть свій текст тут...") },
            shape = RoundedCornerShape(12.dp),
            minLines = 6
        )

        if (isChecked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PocketTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (isEvaluating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PocketTheme.colors.ink,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Очікуємо результат..",
                            style = PocketTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PocketTheme.colors.ink
                        )
                    }
                }

                else if (aiFeedback != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Пояснення:",
                            style = PocketTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = PocketTheme.colors.ink
                        )
                        Text(
                            text = aiFeedback,
                            style = PocketTheme.typography.bodyLarge,
                            color = PocketTheme.colors.ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinishPracticeDialog(
    onRestartClick: () -> Unit,
    onFinishClick: () -> Unit
) {

    val ink: Color = PocketTheme.colors.ink

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = ink,
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()), // Зміщення по X та Y
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()) // Таке ж заокруглення, як у картки
                    )
                }
                .background(PocketTheme.colors.paper, RoundedCornerShape(16.dp))
                .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Практику завершено!",
                    style = PocketTheme.typography.headlineMedium/*.copy(fontWeight = FontWeight.Black)*/,
                    color = PocketTheme.colors.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = "Успішно виконано всі завдання. Результат збережено.",
                    style = PocketTheme.typography.bodyLarge,
                    color = PocketTheme.colors.ink,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PdButton(
                        text = "Повернутися до теорії",
                        onClick = onFinishClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    PdButton(
                        text = "Пройти ще раз",
                        onClick = onRestartClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}