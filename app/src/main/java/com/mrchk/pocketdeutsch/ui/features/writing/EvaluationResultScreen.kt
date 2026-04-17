package com.mrchk.pocketdeutsch.ui.features.writing

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.AiEvaluationResult
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import com.mrchk.pocketdeutsch.ui.components.PdPinnedCard
import com.mrchk.pocketdeutsch.ui.components.PdProgressBar
import com.mrchk.pocketdeutsch.ui.components.PdStickyNote
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun EvaluationResultScreen(
    result: AiEvaluationResult,
    originalText: String,
    selectedCorrection: TextCorrection?,
    onCorrectionClick: (TextCorrection) -> Unit,
    onCloseClick: () -> Unit,
) {

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = "Фідбек від ШІ",
                onBackClick = onCloseClick,
                leftButtonIcon = R.drawable.ic_x_bold,
                onRightButtonClick = { },
                rightButtonIcon = null,
            )
        },
        containerColor = PocketTheme.colors.paper
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ScoreBreakdownCard(result = result)

            OverallFeedbackSection(feedback = result.overallFeedback)


            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Твій текст") // Заміни іконку, якщо треба
                Text(
                    text = "НАТИСНИ НА ПІДСВІЧЕНЕ, ЩОБ ПОБАЧИТИ ПРАВИЛО",
                    style = PocketTheme.typography.labelSmall,
                    color = PocketTheme.colors.gray500,
//                    fontWeight = FontWeight.Bold
                )

                GradedNotepad(
                    originalText = originalText,
                    corrections = result.textCorrections,
                    selectedCorrection = selectedCorrection,
                    onCorrectionClick = onCorrectionClick
                )
            }

            // 3. Картка розбору помилки (Анімована поява)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(title = "Розбір помилки", /*iconRes = R.drawable.ic_magnifying_glass*/) // Заміни іконку
                CorrectionDetailCard(correction = selectedCorrection)
            }

            // 4. Загальний коментар

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

}

@Composable
fun ScoreBreakdownCard(result: AiEvaluationResult) {

    val scoreColor = when {
        result.score >= 80 -> PocketTheme.colors.success
        result.score >= 50 -> Color(0xFFFFBF00)
        else -> PocketTheme.colors.error
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.surface, RoundedCornerShape(20.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Результат перевірки",
                style = PocketTheme.typography.titleMedium
            )
            Text(
                text = "${result.score}/100",
                style = PocketTheme.typography.titleLarge.copy(
                    color = scoreColor
                )
            )
        }

        Divider(color = PocketTheme.colors.gray200, thickness = 2.dp)

        // Прогрес-бари по категоріях
        ScoreProgressBar(
            label = "Граматика",
            score = result.grammarScore,
            color = PocketTheme.colors.primary
        )
        ScoreProgressBar(
            label = "Лексика",
            score = result.vocabularyScore,
            color = PocketTheme.colors.secondary
        )
        ScoreProgressBar(
            label = "Зміст",
            score = result.contentScore,
            color = PocketTheme.colors.success
        )
    }
}

@Composable
fun ScoreProgressBar(label: String, score: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = PocketTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )

        PdProgressBar(
            progress = score / 100f,
            progressColor = color,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun GradedNotepad(
    originalText: String,
    corrections: List<TextCorrection>,
    selectedCorrection: TextCorrection?,
    onCorrectionClick: (TextCorrection) -> Unit,
) {
    val highlightBgColor = PocketTheme.colors.error.copy(alpha = 0.1f)
    val highlightTextColor = PocketTheme.colors.error

    val activeBgColor = PocketTheme.colors.warning
    val activeTextColor = PocketTheme.colors.ink

    val annotatedText = remember(originalText, corrections, highlightBgColor, highlightTextColor) {
        buildAnnotatedString {
            append(originalText)

            corrections.forEachIndexed { index, correction ->
                val targetText = correction.originalIncorrectText.trim()

                val searchStartPoint = maxOf(0, correction.startIndex - 15)
                val realStartIndex = originalText.indexOf(targetText, startIndex = searchStartPoint)

                val finalStart = if (realStartIndex != -1) realStartIndex else correction.startIndex
                val finalEnd =
                    if (realStartIndex != -1) realStartIndex + targetText.length else correction.endIndex

                val safeStart = finalStart.coerceIn(0, originalText.length)
                val safeEnd = finalEnd.coerceIn(safeStart, originalText.length)


                if (safeStart < safeEnd) {
                    val highlightedText = originalText.substring(safeStart, safeEnd)
                }

                if (safeStart != safeEnd) {

                    val isActive = correction == selectedCorrection

                    addStyle(
                        style = SpanStyle(
                            background = if (isActive) activeBgColor else highlightBgColor,
                            color = if (isActive) activeTextColor else highlightTextColor,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isActive) TextDecoration.Underline else TextDecoration.None
                        ),
                        start = safeStart,
                        end = safeEnd
                    )
                    addStringAnnotation(
                        tag = "CORRECTION",
                        annotation = index.toString(),
                        start = safeStart,
                        end = safeEnd
                    )
                }
            }
        }
    }

    val textLineHeightSp = 28.sp
    val density = LocalDensity.current
    val lineHeightPx = with(density) { textLineHeightSp.toPx() }
    val lineColor = PocketTheme.colors.gray200

    PdStickyNote(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp),
        bgColor = PocketTheme.colors.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val baselineOffsetPx = 8.dp.toPx()

                    var y = lineHeightPx - baselineOffsetPx
                    while (y < size.height) {
                        drawLine(
                            color = lineColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineHeightPx
                    }
                },
        ) {
            ClickableText(
                text = annotatedText,
                style = PocketTheme.typography.bodyMedium.copy(
                    lineHeight = textLineHeightSp,
                    color = PocketTheme.colors.ink
                ),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(
                        tag = "CORRECTION",
                        start = offset,
                        end = offset
                    )
                        .firstOrNull()?.let { annotation ->
                            // Дістаємо номер помилки і передаємо її наверх
                            val correctionIndex = annotation.item.toIntOrNull()
                            if (correctionIndex != null && correctionIndex in corrections.indices) {
                                onCorrectionClick(corrections[correctionIndex])
                            }
                        }
                }
            )
        }
    }
}

@Composable
fun OverallFeedbackSection(feedback: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Коментар" /*iconRes = R.drawable.ic_magic_wand*/) // Заміни іконку

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F4F5), RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFFD4D4D8), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = feedback,
                style = PocketTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PocketTheme.colors.ink,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun CorrectionDetailCard(correction: TextCorrection?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .background(PocketTheme.colors.surface, RoundedCornerShape(20.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (correction == null) {
            // Стан до кліку
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pencil_simple_bold), // Заміни іконку
                    contentDescription = null,
                    tint = PocketTheme.colors.gray400,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Обери підсвічений фрагмент тексту, щоб переглянути виправлення.",
                    style = PocketTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = PocketTheme.colors.gray500,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Активний стан з розбором
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .border(width = 2.dp, color = Color.Black)
                    .padding(20.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /*Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA))
                        .border(width = 2.dp, color = Color.Black)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "ТВІЙ ВАРІАНТ",
                            style = PocketTheme.typography.labelSmall,
                            color = PocketTheme.colors.gray500,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x26FF7070), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = correction.originalIncorrectText,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "ЯК ПРАВИЛЬНО",
                            style = PocketTheme.typography.labelSmall,
                            color = PocketTheme.colors.success,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(PocketTheme.colors.success, RoundedCornerShape(8.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = correction.suggestedCorrection,
                                color = PocketTheme.colors.ink,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info_bold), // Заміни іконку
                        contentDescription = "Info",
                        tint = PocketTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = correction.explanation,
                        style = PocketTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }*/
                Text(
                    "Твій текст:",
                    style = PocketTheme.typography.labelSmall,
                    color = PocketTheme.colors.gray500
                )
                Text(
                    text = correction.originalIncorrectText,
                    style = PocketTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.LineThrough, // Залишаємо закреслення для наочності
                        color = PocketTheme.colors.gray500 // Сірий колір (не червоний!), бо це вже минуле
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Пропозиція:",
                    style = PocketTheme.typography.labelSmall,
                    color = PocketTheme.colors.gray500
                )
                Text(
                    text = correction.suggestedCorrection,
                    style = PocketTheme.typography.bodyLarge.copy(
                        color = PocketTheme.colors.primary, // Синій/головний колір додатку замість зеленого
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Пояснення:",
                    style = PocketTheme.typography.labelSmall,
                    color = PocketTheme.colors.gray500
                )
                Text(
                    text = correction.explanation,
                    style = PocketTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String /*iconRes: Int*/) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /*Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = PocketTheme.colors.primary,
        modifier = Modifier.size(20.dp)
    )*/
        Text(
            text = title,
            style = PocketTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/*@Composable
fun AiFeedbackCard(score: Int, feedback: String) {
    val scoreColor = when {
        score >= 80 -> PocketTheme.colors.success
        score >= 50 -> PocketTheme.colors.warning
        else -> PocketTheme.colors.error
    }

    PdPinnedCard(backgroundColor = PocketTheme.colors.surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Результат перевірки",
                style = PocketTheme.typography.titleMedium
            )
            Text(
                text = "$score/100",
                style = PocketTheme.typography.titleLarge.copy(
                    color = scoreColor
                )
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = feedback,
            style = PocketTheme.typography.bodyMedium
        )
    }
}*/
/*
@Composable
fun CorrectionExplanationCard(correction: TextCorrection) {
    // Використовуємо нейтральний фон, наприклад, світло-синій або світло-жовтий
    PdPinnedCard(backgroundColor = PocketTheme.colors.surface) {

        Text("Твій текст:", style = PocketTheme.typography.labelSmall, color = PocketTheme.colors.gray500)
        Text(
            text = correction.originalIncorrectText,
            style = PocketTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.LineThrough, // Залишаємо закреслення для наочності
                color = PocketTheme.colors.gray500 // Сірий колір (не червоний!), бо це вже минуле
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Пропозиція:", style = PocketTheme.typography.labelSmall, color = PocketTheme.colors.gray500)
        Text(
            text = correction.suggestedCorrection,
            style = PocketTheme.typography.bodyLarge.copy(
                color = PocketTheme.colors.primary, // Синій/головний колір додатку замість зеленого
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Пояснення:", style = PocketTheme.typography.labelSmall, color = PocketTheme.colors.gray500)
        Text(
            text = correction.explanation,
            style = PocketTheme.typography.bodyMedium
        )
    }
}*/