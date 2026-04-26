package com.mrchk.pocketdeutsch.ui.features.lesson.theory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.FormsTableDomain
import com.mrchk.pocketdeutsch.domain.model.GrammarRuleDomain
import com.mrchk.pocketdeutsch.ui.components.PdButton // Твоя фірмова кнопка
import com.mrchk.pocketdeutsch.ui.components.PdStickyNote
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar // Твій верхній бар
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun TheoryScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    viewModel: TheoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        containerColor = PocketTheme.colors.paper,
        topBar = {
            PdTitleTopBar(
                title = "Граматика",
                onBackClick = onBackClick,
                onRightButtonClick = { },
                rightButtonIcon = null
            )
        },
        bottomBar = {
            if (uiState is TheoryUiState.Success) {
                // Фіксована панель з кнопкою знизу
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
                    PdButton(
                        text = "Перейти до вправ",
                        onClick = onNextClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TheoryUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )

                is TheoryUiState.Error -> Text(
                    state.message,
                    modifier = Modifier.align(Alignment.Center)
                )

                is TheoryUiState.Success -> {
                    val grammar = state.grammar

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ){
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = grammar.topic,
                                style = PocketTheme.typography.headlineLarge.copy(
                                    fontSize = 28.sp,
                                    lineHeight = 34.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = PocketTheme.colors.ink
                            )
                            Text(
                                text = grammar.explanation,
                                style = PocketTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = PocketTheme.colors.ink.copy(alpha = 0.7f)
                                )
                            )
                        }

                        if (grammar.rules.isNotEmpty()) {
                            Text(
                                text = "Правила",
                                style = PocketTheme.typography.titleLarge,
                                color = PocketTheme.colors.ink
                            )

                            BrutalistCard {
                                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                    grammar.rules.forEachIndexed { index, rule ->
                                        RuleItem(number = index + 1, rule = rule)
                                    }
                                }
                            }
                        }

                        grammar.formsTable?.let { table ->
                            Text(
                                text = "Таблиця відмінювання",
                                style = PocketTheme.typography.titleLarge,
                                color = PocketTheme.colors.ink
                            )
                            GrammarTableComponent(table = table)
                        }

                        if (grammar.warningNotes.isNotEmpty()) {
                            PdStickyNote(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🚨", fontSize = 20.sp)
                                    Text(
                                        text = "Achtung! (Увага)",
                                        style = PocketTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = grammar.warningNotes.joinToString("\n\n"),
                                    style = PocketTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp
                                    )
                                )
                            }
                        }

                        if (grammar.contextExamples.isNotEmpty()) {
                            Text(
                                text = "Приклади в контексті",
                                style = PocketTheme.typography.titleLarge,
                                color = PocketTheme.colors.ink
                            )
                            BrutalistCard {
                                Column {
                                    grammar.contextExamples.forEachIndexed { index, example ->
                                        ContextExampleItem(
                                            text = example,
                                            isLast = index == grammar.contextExamples.lastIndex
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Чорна "тінь" необруталізму
            .offset(x = 4.dp, y = 4.dp)
            .background(PocketTheme.colors.ink, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-4).dp, y = (-4).dp) // Зсуваємо основну картку назад
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun RuleItem(number: Int, rule: GrammarRuleDomain) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Круглий індикатор з номером (Primary колір)
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(PocketTheme.colors.primary, CircleShape)
                .border(2.dp, PocketTheme.colors.ink, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = PocketTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = PocketTheme.colors.ink
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = rule.rule,
                style = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = PocketTheme.colors.ink
            )
            // Блок з прикладом (як у макеті: світлий фон + лінія збоку)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                    .background(PocketTheme.colors.primary.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .matchParentSize()
                        .background(PocketTheme.colors.primary)
                )
                Text(
                    text = rule.example,
                    style = PocketTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 10.dp,
                        bottom = 10.dp,
                        end = 8.dp
                    ),
                    color = PocketTheme.colors.ink
                )
            }
        }
    }
}

@Composable
fun ContextExampleItem(text: String, isLast: Boolean) {
    Column {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Маленька фіолетова крапка
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(10.dp)
                    .background(PocketTheme.colors.primary, CircleShape)
                    .border(1.dp, PocketTheme.colors.ink, CircleShape)
            )
            Text(
                text = text,
                style = PocketTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = PocketTheme.colors.ink
            )
        }
        if (!isLast) {
            // Пунктирний розділювач (можна замінити на Canvas для справжнього пунктиру)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PocketTheme.colors.gray200)
            )
        }
    }
}

@Composable
fun GrammarTableComponent(table: FormsTableDomain) {
    val ink = PocketTheme.colors.ink

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(2.dp, ink, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 1. Заголовки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PocketTheme.colors.gray200)
                .height(IntrinsicSize.Min) // Робимо всі комірки в рядку однієї висоти
        ) {
            table.columns.forEachIndexed { index, header ->
                Box(
                    modifier = Modifier
                        .weight(if (index == 0) 0.9f else 1f) // Трохи збільшили першу колонку
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = header.uppercase(),
                        style = PocketTheme.typography.labelSmall.copy(
                            fontSize = 10.sp, // Трохи збільшили шрифт
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.2.sp
                        ),
                        color = ink,
                        textAlign = TextAlign.Center,
                        maxLines = 2, // Дозволяємо максимум 2 рядки
                        softWrap = true
                    )
                }
                if (index < table.columns.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(ink)
                    )
                }
            }
        }

        // Жирна лінія під заголовком
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(ink)
        )

        // 2. Рядки
        table.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                row.forEachIndexed { cellIndex, cellText ->
                    Box(
                        modifier = Modifier
                            .weight(if (cellIndex == 0) 0.9f else 1f)
                            .background(
                                if (cellIndex == 0) PocketTheme.colors.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .padding(10.dp), // Збільшили відступи всередині клітинок
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = cellText,
                            style = PocketTheme.typography.bodySmall.copy(
                                fontSize = 12.sp, // Більш комфортний розмір
                                fontWeight = if (cellIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                lineHeight = 16.sp
                            ),
                            color = ink
                        )
                    }
                    if (cellIndex < row.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(ink.copy(alpha = 0.3f))
                        )
                    }
                }
            }

            // Тонка лінія між рядками
            if (rowIndex < table.rows.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ink.copy(alpha = 0.1f))
                )
            }
        }
    }
}

enum class GrammarTab {
    THEORY, EXERCISES
}

@Composable
fun PdBrutalistTabs(
    currentTab: GrammarTab,
    onTabSelected: (GrammarTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ink = PocketTheme.colors.ink
    val primary = PocketTheme.colors.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.dp, ink, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)) // Щоб внутрішні фони не вилазили за рамки
    ) {
        // Таб "Теорія"
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (currentTab == GrammarTab.THEORY) primary else Color.Transparent)
                .clickable { onTabSelected(GrammarTab.THEORY) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Теорія",
                style = PocketTheme.typography.titleMedium.copy(
                    fontWeight = if (currentTab == GrammarTab.THEORY) FontWeight.Black else FontWeight.Medium
                ),
                color = ink
            )
        }

        // Вертикальний розділювач
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(ink)
        )

        // Таб "Вправи"
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(if (currentTab == GrammarTab.EXERCISES) primary else Color.Transparent)
                .clickable { onTabSelected(GrammarTab.EXERCISES) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Вправи",
                style = PocketTheme.typography.titleMedium.copy(
                    fontWeight = if (currentTab == GrammarTab.EXERCISES) FontWeight.Black else FontWeight.Medium
                ),
                color = ink
            )
        }
    }
}