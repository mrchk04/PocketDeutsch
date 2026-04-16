package com.mrchk.pocketdeutsch.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun parseHighlightedText(
    text: String,
    highlightColor: Color = PocketTheme.colors.primary,
    highlightBgColor: Color = PocketTheme.colors.primary.copy(alpha = 0.2f)
): AnnotatedString {
    return buildAnnotatedString {

        val parts = text.split("*")

        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        background = highlightBgColor
                    )
                ) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}