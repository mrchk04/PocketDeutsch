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
    // Напівпрозорий фон, як у твоєму css: rgba(165, 148, 249, 0.2)
    highlightBgColor: Color = PocketTheme.colors.primary.copy(alpha = 0.2f)
): AnnotatedString {
    return buildAnnotatedString {
        // Розбиваємо текст по зірочках
        // Наприклад: "Ich *lerne* Deutsch" -> ["Ich ", "lerne", " Deutsch"]
        val parts = text.split("*")

        parts.forEachIndexed { index, part ->
            // Усі непарні індекси (1, 3, 5...) — це текст, який був МІЖ зірочками
            if (index % 2 == 1) {
                withStyle(
                    style = SpanStyle(
                        color = highlightColor,
                        fontWeight = FontWeight.Bold,
                        background = highlightBgColor
                    )
                ) {
                    append(part) // Додаємо підсвічений шматок
                }
            } else {
                // Парні індекси — це звичайний текст
                append(part)
            }
        }
    }
}