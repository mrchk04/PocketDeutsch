package com.mrchk.pocketdeutsch.ui.features.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.ui.components.pdStyle
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun PdWordOfDayCard(
    word: String,
    partOfSpeech: String,
    translation: String,
    example: String? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pdStyle(backgroundColor = PocketTheme.colors.surface, cornerRadius = 24.dp)
            .padding(20.dp)
    ) {
        Column {
            Text(
                text = partOfSpeech,
                color = PocketTheme.colors.gray500,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = word,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PocketTheme.colors.ink
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Справжня пунктирна лінія (Dashed Line)
            val dashColor = PocketTheme.colors.gray400
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                drawLine(
                    color = dashColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 4f, // Товщина лінії
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f) // (довжина штриха, довжина пробілу)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = translation,
                fontSize = 16.sp,
                color = PocketTheme.colors.ink
            )

            if (!example.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“$example”",
                    color = PocketTheme.colors.gray500,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                )
            }
        }
    }
}