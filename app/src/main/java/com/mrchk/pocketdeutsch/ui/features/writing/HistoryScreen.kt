package com.mrchk.pocketdeutsch.ui.features.writing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.data.local.WrittenTaskResultEntity
import com.mrchk.pocketdeutsch.ui.components.PdProgressBar
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onTaskClick: (WrittenTaskResultEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = "Історія перевірок",
                onBackClick = onBackClick,
                leftButtonIcon = R.drawable.ic_arrow_left_bold, // Твоя іконка назад
                onRightButtonClick = { },
                rightButtonIcon = null
            )
        },
        containerColor = PocketTheme.colors.paper
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PocketTheme.colors.primary
                    )
                }
                is HistoryUiState.Empty -> {
                    Text(
                        text = "Ти ще не виконував цю вправу.\nЧас написати перший текст!",
                        style = PocketTheme.typography.bodyLarge,
                        color = PocketTheme.colors.gray500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
                is HistoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.tasks, key = { it.timestamp }) { task ->
                            HistoryCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    task: WrittenTaskResultEntity,
    onClick: () -> Unit
) {
    val scoreColor = when {
        task.overallScore >= 80 -> PocketTheme.colors.success
        task.overallScore >= 50 -> PocketTheme.colors.warning
        else -> PocketTheme.colors.error
    }

    // Форматування дати з timestamp
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(task.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = 4.dp, y = 4.dp) // Жорстка тінь
            .background(PocketTheme.colors.ink, RoundedCornerShape(16.dp))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Дата створення
            Text(
                text = dateString,
                style = PocketTheme.typography.labelSmall,
                color = PocketTheme.colors.gray500,
                fontWeight = FontWeight.Bold
            )

            // Бейдж з оцінкою (повторюємо логіку з головного екрана)
            Box(
                modifier = Modifier
                    .border(3.dp, scoreColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${task.overallScore}",
                    style = PocketTheme.typography.titleMedium,
                    color = PocketTheme.colors.ink,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Прев'ю тексту (щоб користувач згадав, про що писав)
        Text(
            text = task.originalText,
            style = PocketTheme.typography.bodyMedium,
            color = PocketTheme.colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Міні-прогрес-бар для візуалізації
        PdProgressBar(
            progress = task.overallScore / 100f,
            progressColor = scoreColor,
            modifier = Modifier.height(16.dp) // Робимо його тоншим для списку
        )
    }
}