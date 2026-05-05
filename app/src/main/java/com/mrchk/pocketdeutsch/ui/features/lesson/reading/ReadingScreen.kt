package com.mrchk.pocketdeutsch.ui.features.lesson.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.mrchk.pocketdeutsch.domain.model.AdPart
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun ReadingScreen(
    viewModel: ReadingViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
) {
    val data by viewModel.readingData.collectAsState()
    val exerciseIndex by viewModel.currentExerciseIndex.collectAsState() // Індекс вправи (0 або 1)
    val currentIndex by viewModel.currentQuestionIndex.collectAsState() // Індекс питання всередині вправи
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val userTextAnswer by viewModel.userTextAnswer.collectAsState() // Для вправи типу article
    val isChecked by viewModel.isChecked.collectAsState()
    val usedAnswers by viewModel.usedAnswers.collectAsState()

    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val reading = data!!

    val baseExercise = reading.exercises.getOrNull(exerciseIndex) ?: return
    val currentExercise = baseExercise as? InteractiveExercise.Standard ?: return

    if (currentIndex >= currentExercise.items.size) return

    val currentItem = currentExercise.items[currentIndex]
    val correctAnswer = currentExercise.answers[currentIndex]

    // Перевірка, чи це останнє питання ВЗАГАЛІ (в останній вправі)
    val isLastExercise = exerciseIndex == reading.exercises.size - 1
    val isLastQuestionInExercise = currentIndex == currentExercise.items.size - 1
    val isFinalStep = isLastExercise && isLastQuestionInExercise

    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        topBar = {
            val totalItems = reading.exercises.sumOf { exercise ->
                (exercise as? InteractiveExercise.Standard)?.items?.size ?: 0
            }
            val completedInPrevious = reading.exercises.take(exerciseIndex).sumOf { exercise ->
                (exercise as? InteractiveExercise.Standard)?.items?.size ?: 0
            }
            val currentProgress = (completedInPrevious + currentIndex + 1).toFloat() / totalItems

            PdExerciseTopBar(
                progress = currentProgress,
                progressText = "${completedInPrevious + currentIndex + 1}/$totalItems",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PocketTheme.colors.paper)
                    .drawBehind {
                        drawLine(
                            color = ink,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
            ) {
                if (isChecked) {
                    PdButton(
                        text = if (isFinalStep) "Завершити" else "Далі",
                        onClick = {
                            if (isFinalStep) onComplete() else viewModel.nextQuestion()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Кнопка активна, якщо обрано варіант АБО введено текст
                    val canCheck = selectedAnswer != null || userTextAnswer.isNotBlank()

                    PdButton(
                        text = "Перевірити",
                        onClick = { viewModel.checkAnswer() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canCheck
                    )
                }
            }
        },
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            when (currentExercise.type) {
                "advertisements", "information_extraction" -> {
                    AdvertisementExercise(
                        instruction = currentExercise.instruction,
                        adParts = reading.adParts ?: emptyList(),
                        currentItem = currentItem,
                        correctAnswer = correctAnswer,
                        selectedAnswer = selectedAnswer,
                        isChecked = isChecked,
                        onAnswerSelect = { viewModel.selectAnswer(it) }
                    )
                }

                "article" -> {
                    // Новий відокремлений компонент для вільного вводу
                    FreeTextReadingExercise(
                        instruction = currentExercise.instruction,
                        text = reading.text,
                        question = currentItem,
                        userAnswer = userTextAnswer,
                        correctAnswer = correctAnswer,
                        isChecked = isChecked,
                        onValueChange = { viewModel.onUserTextChange(it) }
                    )
                }

                "multiple_choice" -> {
                    ClassicReadingExercise(
                        instruction = currentExercise.instruction,
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
                        instruction = currentExercise.instruction,
                        text = reading.text,
                        currentItem = currentItem,
                        correctAnswer = correctAnswer,
                        selectedAnswer = selectedAnswer,
                        isChecked = isChecked,
                        usedAnswers = usedAnswers,
                        onAnswerSelect = { viewModel.selectAnswer(it) }
                    )
                }

                else -> {
                    Text(
                        text = "Невідомий тип вправи: ${currentExercise.type}",
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
    optionsContent: @Composable () -> Unit,
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
    adParts: List<AdPart>, // <--- ПЕРЕДАЄМО СПИСОК ОБ'ЄКТІВ
    currentItem: String,
    correctAnswer: String,
    selectedAnswer: String?,
    isChecked: Boolean,
    onAnswerSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = instruction,
            style = PocketTheme.typography.titleMedium,
            color = PocketTheme.colors.ink,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Горизонтальний скрол для оголошень
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            adParts.forEach { ad ->
                AdvertisementCard(
                    letter = ad.letter, // Беремо літеру безпосередньо з об'єкта
                    content = ad.content,
                    modifier = Modifier.width(280.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Блок питання та кнопок залишається майже без змін
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PocketTheme.colors.paper)
        ) {
            Text(
                text = currentItem,
                style = PocketTheme.typography.titleMedium,
                color = PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Варіанти тепер включають C та Keine, як у новому JSON
            val options = listOf("A", "B", "C", "Keine")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionButton(
                        text = "A",
                        isSelected = selectedAnswer == "A",
                        isChecked = isChecked,
                        isCorrect = correctAnswer.contains("A"),
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect("A") }
                    OptionButton(
                        text = "B",
                        isSelected = selectedAnswer == "B",
                        isChecked = isChecked,
                        isCorrect = correctAnswer.contains("B"),
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect("B") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionButton(
                        text = "C",
                        isSelected = selectedAnswer == "C",
                        isChecked = isChecked,
                        isCorrect = correctAnswer.contains("C"),
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect("C") }
                    OptionButton(
                        text = "Keine",
                        isSelected = selectedAnswer == "Keine",
                        isChecked = isChecked,
                        isCorrect = correctAnswer == "Keine",
                        modifier = Modifier.weight(1f)
                    ) { onAnswerSelect("Keine") }
                }
            }
        }
    }
}

@Composable
fun AdvertisementCard(
    letter: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    val ink = PocketTheme.colors.ink
    Column(
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
            .padding(16.dp)
    ) {
        // Яскравий маркер літери (A, B або C)
        Box(
            modifier = Modifier
                .background(PocketTheme.colors.tertiary, RoundedCornerShape(8.dp))
                .border(1.dp, ink, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Anzeige $letter",
                style = PocketTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ink
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = content,
            style = PocketTheme.typography.bodyMedium,
            color = ink,
            lineHeight = 22.sp
        )
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
    onAnswerSelect: (String) -> Unit,
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

@Composable
fun FreeTextReadingExercise(
    instruction: String,
    text: String,
    question: String,
    userAnswer: String,
    correctAnswer: String,
    isChecked: Boolean,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
    ) {
        Text(text = instruction, style = PocketTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        ReadingTextCard(
            text = text,
            modifier = Modifier.height(360.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Text(text = question, style = PocketTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = userAnswer,
                onValueChange = onValueChange,
                enabled = !isChecked,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        PocketTheme.colors.ink,
                        RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = PocketTheme.colors.surface,
                    unfocusedContainerColor = PocketTheme.colors.surface,
                    disabledContainerColor = PocketTheme.colors.surface,
                    disabledTextColor = PocketTheme.colors.ink,
                ),
                textStyle = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                placeholder = { Text("Впишіть відповідь...") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (isChecked) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        PocketTheme.colors.success.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(2.dp, PocketTheme.colors.success, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Mögliche Antwort (Еталон):",
                        style = PocketTheme.typography.labelSmall,
                        color = PocketTheme.colors.success
                    )
                    Text(text = correctAnswer, style = PocketTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun MatchingHeadingsExercise(
    instruction: String,
    text: String,
    currentItem: String,
    correctAnswer: String,
    selectedAnswer: String?,
    isChecked: Boolean,
    usedAnswers: Set<String>,
    onAnswerSelect: (String) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
    modifier: Modifier = Modifier,
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
                        .background(
                            PocketTheme.colors.warning.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
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
    onClick: () -> Unit,
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
    val borderColor =
        if (isSelected || (isChecked && isCorrect)) PocketTheme.colors.ink else PocketTheme.colors.gray400
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