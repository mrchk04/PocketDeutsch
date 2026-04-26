package com.mrchk.pocketdeutsch.ui.features.lesson.language_use

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.domain.model.GapOption
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

/*
@Composable
fun InteractiveGapText(
    textWithGaps: String,
    selectedAnswers: Map<Int, String>,
    isChecked: Boolean,
    exerciseItems: List<GapOption>,
    onGapClick: (Int) -> Unit
) {
    val gapRegex = Regex("\\[(\\d+)\\]")
    val matches = gapRegex.findAll(textWithGaps).toList()

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { match ->
            append(textWithGaps.substring(lastIndex, match.range.first))
            val gapNumber = match.groupValues[1].toInt()
            appendInlineContent(id = "gap_$gapNumber", alternateText = "[?]")
            lastIndex = match.range.last + 1
        }
        append(textWithGaps.substring(lastIndex))
    }

    val inlineContent = matches.associate { match ->
        val num = match.groupValues[1].toInt()
        val answer = selectedAnswers[num]
        val isCorrect = isChecked && answer == exerciseItems.find { it.gapNumber == num }?.correctAnswer

        "gap_$num" to InlineTextContent(
            Placeholder(
                width = 5.5.em,
                height = 1.8.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .background(
                        color = when {
                            !isChecked && answer != null -> PocketTheme.colors.warning.copy(alpha = 0.3f)
                            isChecked && isCorrect -> PocketTheme.colors.success.copy(alpha = 0.3f)
                            isChecked && !isCorrect -> PocketTheme.colors.error.copy(alpha = 0.3f)
                            else -> PocketTheme.colors.gray200
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (answer != null) 2.dp else 1.dp,
                        color = when {
                            isChecked && isCorrect -> PocketTheme.colors.success
                            isChecked && !isCorrect -> PocketTheme.colors.error
                            answer != null -> PocketTheme.colors.ink
                            else -> PocketTheme.colors.gray400
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onGapClick(num) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = answer?.take(2) ?: "[$num]", // Показуємо "A)" або номер пропуску
                    style = PocketTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = PocketTheme.colors.ink
                )
            }
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = PocketTheme.typography.bodyLarge.copy(
            lineHeight = 38.sp, // Великий інтервал, щоб влізи рамки
            fontSize = 19.sp
        )
    )
}*/

@Composable
fun InteractiveGapText(
    textWithGaps: String,
    selectedAnswers: Map<Int, String>,
    isChecked: Boolean,
    exerciseItems: List<GapOption>,
    activeGapNumber: Int?, // Змінна для відстеження відкритого меню
    onGapClick: (Int) -> Unit,
    onOptionSelect: (Int, String) -> Unit, // Обробник вибору
    onDismissMenu: () -> Unit // Обробник закриття
) {
    val gapRegex = Regex("\\[(\\d+)\\]")
    val matches = gapRegex.findAll(textWithGaps).toList()

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { match ->
            append(textWithGaps.substring(lastIndex, match.range.first))
            val gapNumber = match.groupValues[1].toInt()
            appendInlineContent(id = "gap_$gapNumber", alternateText = "[?]")
            lastIndex = match.range.last + 1
        }
        append(textWithGaps.substring(lastIndex))
    }

    val inlineContent = matches.associate { match ->
        val num = match.groupValues[1].toInt()
        val answer = selectedAnswers[num]
        val isCorrect = isChecked && answer == exerciseItems.find { it.gapNumber == num }?.correctAnswer
        val isMenuExpanded = activeGapNumber == num // Перевіряємо, чи відкрите меню для цього пропуску

        "gap_$num" to InlineTextContent(
            Placeholder(width = 6.em, height = 2.em, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .background(
                        color = when {
                            !isChecked && answer != null -> PocketTheme.colors.warning.copy(alpha = 0.3f)
                            isChecked && isCorrect -> PocketTheme.colors.success.copy(alpha = 0.3f)
                            isChecked && !isCorrect -> PocketTheme.colors.error.copy(alpha = 0.3f)
                            else -> PocketTheme.colors.gray200
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = if (answer != null) 2.dp else 1.dp,
                        color = when {
                            isChecked && isCorrect -> PocketTheme.colors.success
                            isChecked && !isCorrect -> PocketTheme.colors.error
                            answer != null -> PocketTheme.colors.ink
                            else -> PocketTheme.colors.gray400
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onGapClick(num) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = answer?.take(2) ?: "[$num]",
                    style = PocketTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = PocketTheme.colors.ink
                )

                // Випадаючий список, прив'язаний до цього Box
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier
                        .background(Color.White)
                        .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(8.dp))
                ) {
                    val options = exerciseItems.find { it.gapNumber == num }?.options ?: emptyList()
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = PocketTheme.typography.bodyMedium
                                )
                            },
                            onClick = { onOptionSelect(num, option) }
                        )
                    }
                }
            }
        }
    }

    Text(
        text = annotatedString,
        inlineContent = inlineContent,
        style = PocketTheme.typography.bodyLarge.copy(
            lineHeight = 38.sp,
            fontSize = 19.sp
        )
    )
}
