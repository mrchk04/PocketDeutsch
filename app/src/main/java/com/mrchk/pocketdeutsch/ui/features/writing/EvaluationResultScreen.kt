package com.mrchk.pocketdeutsch.ui.features.writing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun EvaluationResultScreen (
    result: AiEvaluationResult,
    originalText: String,
    selectedCorrection: TextCorrection?,
    onCorrectionClick: (TextCorrection) -> Unit,
    onCloseClick: () -> Unit,
) {

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = "Ergebnis",
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
            AiFeedbackCard(score = result.score, feedback = result.overallFeedback)

            GradedNotepad(
                originalText = originalText,
                corrections = result.textCorrections,
                onCorrectionClick = onCorrectionClick
            )
            selectedCorrection?.let { correction ->
                CorrectionExplanationCard(correction = correction)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

}

@Composable
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
                text = "Результат перевірки", // Або "AI-Аналіз", "Твій результат"
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

}


@Composable
fun GradedNotepad(
    originalText: String,
    corrections: List<TextCorrection>,
    onCorrectionClick: (TextCorrection) -> Unit
) {
    val highlightBgColor = PocketTheme.colors.error.copy(alpha = 0.1f)
    val highlightTextColor = PocketTheme.colors.error

    val annotatedText = remember(originalText, corrections, highlightBgColor, highlightTextColor) {
        buildAnnotatedString {
            append(originalText)

            corrections.forEachIndexed { index, correction ->
                addStyle(
                    style = SpanStyle(
                        background = highlightBgColor,
                        color = highlightTextColor
                    ),
                    start = correction.startIndex,
                    end = correction.endIndex
                )
                addStringAnnotation(
                    tag = "CORRECTION",
                    annotation = index.toString(),
                    start = correction.startIndex,
                    end = correction.endIndex
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.paper)
            .padding(16.dp)
    ) {
        ClickableText(
            text = annotatedText,
            style = PocketTheme.typography.bodyMedium.copy(
                lineHeight = 28.sp,
                color = PocketTheme.colors.ink
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations(tag = "CORRECTION", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val clickedCorrectionIndex = annotation.item.toInt()
                        onCorrectionClick(corrections[clickedCorrectionIndex])
                    }
            }
        )
    }
}

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
}