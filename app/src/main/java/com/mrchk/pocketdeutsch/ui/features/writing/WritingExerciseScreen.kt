package com.mrchk.pocketdeutsch.ui.features.writing

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdChecklistItem
import com.mrchk.pocketdeutsch.ui.components.PdNotepadInput
import com.mrchk.pocketdeutsch.ui.components.PdPhraseChip
import com.mrchk.pocketdeutsch.ui.components.PdPinnedCard
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WritingExerciseScreen(
    viewModel: WritingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToHistory: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Crossfade(targetState = state.result, label = "ScreenTransition") { result ->
        if (result != null) {
            EvaluationResultScreen(
                result = result,
                originalText = state.textInput,
                selectedCorrection = state.selectedCorrection,
                onCorrectionClick = viewModel::onCorrectionSelected,
                onCloseClick = { viewModel.resetEvaluation() }
            )
        } else {
            WritingContent(
                state = state,
                onTextChanged = viewModel::onTextChanged,
                onCheckToggle = viewModel::onChecklistItemToggled,
                onHintClick = viewModel::onRedemittelClicked,
                onSubmit = viewModel::submitForEvaluation,
                onBackClick = onNavigateBack,
                onErrorDismiss = viewModel::clearError,
                onHistoryClick = {
                    state.task?.id?.let { taskId ->
                        onNavigateToHistory(taskId)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingContent(
    state: WritingUiState,
    onTextChanged: (String) -> Unit,
    onCheckToggle: (String, Boolean) -> Unit,
    onHintClick: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackClick: () -> Unit,
    onErrorDismiss: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onErrorDismiss()
        }
    }

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = state.task?.title ?: "Schreiben",
                onBackClick = onBackClick,
                onRightButtonClick = {},
                rightButtonIcon = R.drawable.ic_lightbulb_bold,
                rightButtonIconColor = PocketTheme.colors.warning,
            )
        },
        bottomBar = {
            if (!imeVisible) {
                WritingBottomActionBar(
                    onCheckClick = onSubmit,
                    onHistoryClick = onHistoryClick,
                    state = state
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(PocketTheme.colors.warning)
                        .border(2.dp, PocketTheme.colors.ink)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ){
                    Text(
                        text = data.visuals.message,
                        color = PocketTheme.colors.ink,
                        style = PocketTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        containerColor = PocketTheme.colors.paper,
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            state.task?.let { task ->
                PdPinnedCard {
                    Text("Aufgabe:", style = PocketTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(task.promptText, style = PocketTheme.typography.bodyMedium)
                }
            }

            if (state.checklist.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Напишіть згідно з цими пунктами:",
                        style = PocketTheme.typography.labelSmall,
                        color = PocketTheme.colors.gray500,
                    )

                    state.checklist.forEach { item ->
                        PdChecklistItem(
                            text = item.text,
                            isChecked = item.isChecked,
                            onCheckedChange = { isChecked -> onCheckToggle(item.id, isChecked) }
                        )
                    }
                }
            }

            if (!state.task?.hints.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "REDEMITTEL (TAP TO ADD):",
                        style = PocketTheme.typography.labelSmall,
                        color = PocketTheme.colors.gray500,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.task!!.hints.forEach { hint ->
                            PdPhraseChip(
                                text = hint.trim(),
                                onClick = { onHintClick(hint) }
                            )
                        }
                    }
                }
            }

            PdNotepadInput(
                text = state.textInput,
                onValueChange = onTextChanged,
                wordCount = state.wordCount,
                onExpandClick = {}
            )
        }
    }
}

@Composable
fun WritingBottomActionBar(
    onCheckClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: WritingUiState,
    ) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.paper) // Або surface, залежно від фону
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 1. Кнопка "Історія" (Квадратна, другорядна, але помітна)
        Box(
            modifier = Modifier
                .size(56.dp) // Зручний розмір для тапу
                .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
                .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onHistoryClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_pencil_simple_bold), // Заміни на свою іконку
                contentDescription = "Історія перевірок",
                tint = PocketTheme.colors.ink,
                modifier = Modifier.size(28.dp)
            )
        }

        PdButton(
            text = if (state.isLoading) "Завантаження..." else "Надіслати",
            enabled = !state.isLoading && state.textInput.isNotBlank(),
            onClick = onCheckClick,
            modifier = Modifier.weight(1f)
        )
    }
}