package com.mrchk.pocketdeutsch.ui.features.lesson.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun ReadingScreen(
    viewModel: ReadingViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit
) {
    val data by viewModel.readingData.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()
    val usedAnswers by viewModel.usedAnswers.collectAsState()

    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val reading = data!!
    val exercise = reading.exercise

    if (currentIndex >= exercise.items.size) return

    val currentItem = exercise.items[currentIndex]
    val correctAnswer = exercise.answers[currentIndex]
    val isLastQuestion = currentIndex == exercise.items.size - 1

    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        topBar = {
            PdExerciseTopBar(
                progress = (currentIndex + 1).toFloat() / exercise.items.size,
                progressText = "${currentIndex + 1}/${exercise.items.size}",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PocketTheme.colors.paper)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawLine(
                            color = ink,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
            ) {
                if (isChecked) {
                    PdButton(
                        text = if (isLastQuestion) "Завершити" else "Далі",
                        onClick = {
                            if (isLastQuestion) onComplete() else viewModel.nextQuestion()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PdButton(
                        text = "Перевірити",
                        onClick = { viewModel.checkAnswer() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedAnswer != null
                    )
                }
            }
        },
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        // Головний контейнер на весь екран
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            when (exercise.type) {
                "information_extraction" -> {
                    AdvertisementExercise(
                        instruction = exercise.instruction,
                        text = reading.text,
                        currentItem = currentItem,
                        correctAnswer = correctAnswer,
                        selectedAnswer = selectedAnswer,
                        isChecked = isChecked,
                        onAnswerSelect = { viewModel.selectAnswer(it) }
                    )
                }
                "multiple_choice" -> {
                    ClassicReadingExercise(
                        instruction = exercise.instruction,
                        text = reading.text,
                        currentItem = currentItem,
                        correctAnswer = correctAnswer,
                        selectedAnswer = selectedAnswer,
                        isChecked = isChecked,
                        onAnswerSelect = { viewModel.selectAnswer(it) }
                    )
                }
                "matching_headings" -> {
                    MatchingHeadingsExercise(
                        instruction = exercise.instruction,
                        text = reading.text,
                        currentItem = currentItem,
                        correctAnswer = correctAnswer,
                        selectedAnswer = selectedAnswer,
                        isChecked = isChecked,
                        usedAnswers = usedAnswers, // ПЕРЕДАЄМО СЮДИ
                        onAnswerSelect = { viewModel.selectAnswer(it) }
                    )
                }
                else -> {
                    Text(
                        text = "Невідомий тип вправи: ${exercise.type}",
                        color = PocketTheme.colors.error
                    )
                }
            }
        }
    }
}

// --- УНІВЕРСАЛЬНИЙ КАРКАС ---
@Composable
fun ExerciseLayoutShell(
    instruction: String,
    text: String,
    headings: String? = null, // НОВЕ ПОЛЕ ДЛЯ ЗАГОЛОВКІВ
    currentItem: String,
    optionsContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = instruction,
            style = PocketTheme.typography.titleLarge,
            color = PocketTheme.colors.ink,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ReadingTextCard(
            text = text,
            headings = headings,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = currentItem,
            style = PocketTheme.typography.titleLarge,
            color = PocketTheme.colors.ink
        )

        Spacer(modifier = Modifier.height(20.dp))

        optionsContent()
    }
}

// --- ТИП 1: Оголошення (A, B, Beide, Keine) ---
@Composable
fun AdvertisementExercise(
    instruction: String,
    text: String,
    currentItem: String,
    correctAnswer: String,
    selectedAnswer: String?,
    isChecked: Boolean,
    onAnswerSelect: (String) -> Unit
) {
    val options = listOf("A", "B", "Beide", "Keine")

    ExerciseLayoutShell(
        instruction = instruction,
        text = text,
        currentItem = currentItem
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OptionButton(text = options[0], isSelected = selectedAnswer == options[0], isChecked = isChecked, isCorrect = options[0] == correctAnswer, modifier = Modifier.weight(1f)) { onAnswerSelect(options[0]) }
                OptionButton(text = options[1], isSelected = selectedAnswer == options[1], isChecked = isChecked, isCorrect = options[1] == correctAnswer, modifier = Modifier.weight(1f)) { onAnswerSelect(options[1]) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OptionButton(text = options[2], isSelected = selectedAnswer == options[2], isChecked = isChecked, isCorrect = options[2] == correctAnswer, modifier = Modifier.weight(1f)) { onAnswerSelect(options[2]) }
                OptionButton(text = options[3], isSelected = selectedAnswer == options[3], isChecked = isChecked, isCorrect = options[3] == correctAnswer, modifier = Modifier.weight(1f)) { onAnswerSelect(options[3]) }
            }
        }
    }
}

// --- ТИП 2: Класичне читання (A, B, C) ---
@Composable
fun ClassicReadingExercise(
    instruction: String,
    text: String,
    currentItem: String,
    correctAnswer: String,
    selectedAnswer: String?,
    isChecked: Boolean,
    onAnswerSelect: (String) -> Unit
) {
    val options = listOf("A", "B", "C")

    ExerciseLayoutShell(
        instruction = instruction,
        text = text,
        currentItem = currentItem
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEach { option ->
                OptionButton(
                    text = "Варіант $option",
                    isSelected = selectedAnswer == option,
                    isChecked = isChecked,
                    isCorrect = option == correctAnswer,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAnswerSelect(option) }
                )
            }
        }
    }
}

// --- ТИП 3: Підбір заголовків (A, B, C, D, E, F...) ---
@Composable
fun MatchingHeadingsExercise(
    instruction: String,
    text: String,
    currentItem: String,
    correctAnswer: String,
    selectedAnswer: String?,
    isChecked: Boolean,
    usedAnswers: Set<String>,
    onAnswerSelect: (String) -> Unit
) {
    val options = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    // РОЗРІЗАЄМО ТЕКСТ НА ДВІ ЧАСТИНИ
    val parts = text.split("---", limit = 2)
    val headingsText = if (parts.size > 1) parts[0].trim() else null
    val mainText = if (parts.size > 1) parts[1].trim() else text

    ExerciseLayoutShell(
        instruction = instruction,
        text = mainText,       // Передаємо очищений текст
        headings = headingsText, // Передаємо виділені заголовки
        currentItem = "Виберіть заголовок для: $currentItem"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in 0..3) {
                    OptionButton(
                        text = options[i],
                        isSelected = selectedAnswer == options[i],
                        isChecked = isChecked,
                        isCorrect = options[i] == correctAnswer,
                        isUsed = usedAnswers.contains(options[i]),
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect(options[i]) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in 4..7) {
                    OptionButton(
                        text = options[i],
                        isSelected = selectedAnswer == options[i],
                        isChecked = isChecked,
                        isCorrect = options[i] == correctAnswer,
                        isUsed = usedAnswers.contains(options[i]),
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect(options[i]) }
                }
            }
        }
    }
}

@Composable
fun ReadingTextCard(
    text: String,
    headings: String? = null, // НОВЕ ПОЛЕ ДЛЯ ЗАГОЛОВКІВ
    modifier: Modifier = Modifier
) {
    val ink = PocketTheme.colors.ink
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(2.dp, ink, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ЯКЩО Є ЗАГОЛОВКИ - МАЛЮЄМО ЇХ В ОКРЕМІЙ ПЛАШЦІ
            if (headings != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PocketTheme.colors.warning.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .border(2.dp, PocketTheme.colors.warning, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = headings,
                        style = PocketTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PocketTheme.colors.ink,
                        lineHeight = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // САМ ТЕКСТ
            Text(
                text = text,
                style = PocketTheme.typography.bodyLarge,
                color = ink,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean,
    isUsed: Boolean = false, // ДОДАЛИ ПАРАМЕТР ЗА ЗАМОВЧУВАННЯМ
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isUsed -> PocketTheme.colors.gray200 // Світло-сірий фон для використаних
        isChecked && isCorrect -> PocketTheme.colors.success
        isChecked && isSelected && !isCorrect -> PocketTheme.colors.error
        !isChecked && isSelected -> PocketTheme.colors.tertiary
        else -> PocketTheme.colors.surface
    }

    // Тінь і рамка для використаних кнопок теж стають сірими, щоб кнопка "впала" на фон
    val shadowColor = if (isUsed) PocketTheme.colors.gray400 else PocketTheme.colors.ink
    val borderColor = if (isSelected || (isChecked && isCorrect)) PocketTheme.colors.ink else PocketTheme.colors.gray400
    val textColor = if (isUsed) PocketTheme.colors.gray400 else PocketTheme.colors.ink

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = shadowColor, // Динамічна тінь
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isChecked && !isUsed) { onClick() } // БЛОКУЄМО КЛІК ЯКЩО ВИКОРИСТАНО
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = PocketTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor // Динамічний текст
        )
    }
}