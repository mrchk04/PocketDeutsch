package com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrchk.pocketdeutsch.domain.model.CollocationUi
import com.mrchk.pocketdeutsch.domain.model.Word
import com.mrchk.pocketdeutsch.ui.components.BrutalistCard
import com.mrchk.pocketdeutsch.ui.components.ContextExampleItem
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import kotlinx.coroutines.delay

@Composable
fun VocabularyScreen(
    viewModel: VocabularyViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit // Перехід до вправ після вивчення слів
) {

    var timeSpentSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            timeSpentSeconds++
        }
    }
    val vocabulary by viewModel.vocabData.collectAsState()

    if (vocabulary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val data = vocabulary!!

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = "Лексика",
                onBackClick = onBackClick,
                onRightButtonClick = { },
                rightButtonIcon = null
            )
        },
        bottomBar = {
            val ink: Color = PocketTheme.colors.ink
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
                PdButton(
                    text = "Перейти до вправ",
                    onClick = {
                        // 1. ЗБЕРІГАЄМО ЗВІТ ПРО ЧАС ВИВЧЕННЯ СЛІВ
                        com.mrchk.pocketdeutsch.utils.TestAnalytics.addReport(
                            com.mrchk.pocketdeutsch.utils.ExerciseReport(
                                exerciseName = "Лексика (Ознайомлення зі словами)",
                                timeSpentSeconds = timeSpentSeconds,
                                correctAnswers = 0, // Немає автоперевірки
                                totalQuestions = 0
                            )
                        )

                        // 2. ЙДЕМО ДАЛІ
                        onNextClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = PocketTheme.colors.paper
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Блок зі словами
            if (data.words.isNotEmpty()) {
                SectionTitle(title = "Нові слова")
                data.words.forEach { word ->
                    WordCard(word = word)
                }
            }

            // 2. Блок зі сталими виразами
            if (data.collocations.isNotEmpty()) {
                SectionTitle(title = "Сталі вирази")
                data.collocations.forEach { collocation ->
                    CollocationCard(collocation = collocation)
                }
            }

            // 3. Слова в контексті
            if (data.contextSentences.isNotEmpty()) {
                SectionTitle(title = "Приклади в контексті")
                ContextCard(sentences = data.contextSentences)
            }

            // Відступ знизу, щоб контент не перекривався bottomBar
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- КОМПОНЕНТИ ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = PocketTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = PocketTheme.colors.ink,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun WordCard(word: Word, modifier: Modifier = Modifier) {
    val ink = PocketTheme.colors.ink

    val cleanWord = if (!word.article.isNullOrBlank() && word.word.startsWith(word.article, ignoreCase = true)) {
        word.word.substring(word.article.length).trim()
    } else {
        word.word
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
            .border(2.dp, ink, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Кольоровий бейджик
                if (!word.article.isNullOrBlank()) {
                    val articleColor = when (word.article.lowercase()) {
                        "der" -> PocketTheme.colors.primary.copy(0.8f)
                        "die" -> PocketTheme.colors.error.copy(0.8f)
                        "das" -> PocketTheme.colors.success.copy(0.8f)
                        else -> PocketTheme.colors.tertiary
                    }
                    Box(
                        modifier = Modifier
                            .background(articleColor, RoundedCornerShape(4.dp)) // Менше заокруглення (4.dp замість 6.dp)
                            .border(1.dp, ink, RoundedCornerShape(4.dp)) // ТОНША РАМКА (1.dp замість 2.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp) // Робимо його компактнішим
                    ) {
                        Text(
                            text = word.article,
                            style = PocketTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), // Зменшили шрифт артикля
                            color = ink
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // Трохи більше повітря між бейджем і словом
                }

                Text(
                    text = cleanWord,
                    style = PocketTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ink
                )

                if (!word.plural.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${word.plural})",
                        style = PocketTheme.typography.bodyMedium,
                        color = PocketTheme.colors.gray500
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = word.translation,
                style = PocketTheme.typography.bodyLarge,
                color = ink
            )

            if (word.example.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "«${word.example}»",
                    style = PocketTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = PocketTheme.colors.gray500
                )
            }
        }
    }
}

@Composable
fun CollocationCard(collocation: CollocationUi, modifier: Modifier = Modifier) {
    val ink = PocketTheme.colors.ink
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
            .border(2.dp, ink, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = collocation.phrase,
                style = PocketTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = ink
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = collocation.translation,
                style = PocketTheme.typography.bodyLarge,
                color = ink
            )

            if (collocation.example.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "«${collocation.example}»",
                    style = PocketTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = PocketTheme.colors.gray500
                )
            }
        }
    }
}

@Composable
fun ContextCard(sentences: List<String>, modifier: Modifier = Modifier) {
    BrutalistCard(modifier = modifier) {
        Column {
            sentences.forEachIndexed { index, sentence ->
                ContextExampleItem(
                    text = sentence,
                    isLast = index == sentences.lastIndex
                )
            }
        }
    }
}