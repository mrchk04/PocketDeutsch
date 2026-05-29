package com.mrchk.pocketdeutsch.ui.features.lesson.details

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.Lesson
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdIconButton
import com.mrchk.pocketdeutsch.ui.components.PdProgressBar
import com.mrchk.pocketdeutsch.ui.components.PdProgressSize
import com.mrchk.pocketdeutsch.ui.components.TopBarContainer
import com.mrchk.pocketdeutsch.ui.components.pdClickable
import com.mrchk.pocketdeutsch.ui.components.pdStyle
import com.mrchk.pocketdeutsch.ui.theme.Gray400
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

enum class NodeState { COMPLETED, ACTIVE, NOT_STARTED }

data class PathwayNodeData(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val state: NodeState,
    val type: String,
)

@Composable
fun CoursePathwayScreen(
    onBackClick: () -> Unit,
    onNodeClick: (String, String) -> Unit,
    viewModel: LessonDetailViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsState()

    Crossfade(targetState = uiState, label = "ScreenState") { state ->
        when (state) {
            is LessonDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PocketTheme.colors.primary)
                }
            }
            is LessonDetailState.Success -> {
                PathwayContent(
                    lesson = state.lesson,
                    nodes = state.nodes,
                    onBackClick = onBackClick,
                    onNodeClick = onNodeClick,
                    onContinueClick = {
                        val nextTask = state.nodes.firstOrNull { it.state != NodeState.COMPLETED }
                            ?: state.nodes.last()
                        onNodeClick(nextTask.id, nextTask.type)
                    }
                )
            }
            is LessonDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = PocketTheme.colors.error)
                }
            }
        }
    }
}

@Composable
private fun PathwayContent(
    lesson: Lesson,
    nodes: List<PathwayNodeData>,
    onBackClick: () -> Unit,
    onNodeClick: (String, String) -> Unit,
    onContinueClick: () -> Unit
) {
    val ink: Color = PocketTheme.colors.ink

    val context = LocalContext.current

    val totalLessons = nodes.size
    val completedLessons = nodes.count { it.state == NodeState.COMPLETED }
    val currentProgress = if (totalLessons > 0) completedLessons.toFloat() / totalLessons.toFloat() else 0f

    val isAllCompleted = totalLessons > 0 && completedLessons == totalLessons

    var showTestingMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PocketTheme.colors.paper,
        topBar = {
            TopBarContainer(isDashed = false) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    PdIconButton(
                        iconRes = R.drawable.ic_arrow_left_bold,
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Text(
                        text = lesson.topic,
                        style = PocketTheme.typography.titleLarge,
                        color = PocketTheme.colors.ink,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        bottomBar = {
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
//                     Відступ знизу для навігаційного бару системи
                    .navigationBarsPadding()

            ) {
                PdButton(
                    text = if (isAllCompleted) "Завершити тестування" else "Продовжити Розділ",
                    onClick = {
                        if (isAllCompleted) {
                            showTestingMenu = true
                        } else {
                            onContinueClick()
                        }
                    },
                    backgroundColor = PocketTheme.colors.ink,
                    iconRes = if (isAllCompleted) null else R.drawable.ic_arrow_right_bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        contentWindowInsets = WindowInsets(0),
        modifier = Modifier.imePadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            val totalLessons = nodes.size
            val completedLessons = nodes.count { it.state == NodeState.COMPLETED }
            val currentProgress = if (totalLessons > 0) completedLessons.toFloat() / totalLessons.toFloat() else 0f

            UnitHeader(
                unitNumber = "Рівень ${lesson.level}",
                unitTitle = lesson.title,
                unitDescription = lesson.shortDescription ?: "Опануйте нову лексику та граматику.",
                completedLessons = completedLessons,
                totalLessons = totalLessons,
                progress = currentProgress
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            val lineX = 24.dp.toPx() // Центр 48dp іконки
                            drawLine(
                                color = Gray400,
                                start = Offset(lineX, 32.dp.toPx()),
                                end = Offset(lineX, size.height - 32.dp.toPx()),
                                strokeWidth = 4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    nodes.forEach { node ->
                        PathwayNodeItem(
                            data = node,
                            onClick = { onNodeClick(node.id, node.type) }
                        )
                    }
                }
            }
        }
    }

    if (showTestingMenu) {
        Dialog(onDismissRequest = { showTestingMenu = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRoundRect(
                            color = ink,
                            topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx())
                        )
                    }
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(2.dp, ink, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Розділ пройдено!",
                        style = PocketTheme.typography.titleLarge,
                        color = PocketTheme.colors.ink,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Дякую за участь у тестуванні застосунку PocketDeutsch.",
                        style = PocketTheme.typography.bodyMedium,
                        color = PocketTheme.colors.ink,
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    PdButton(
                        text = "1. Надіслати звіт (CSV)",
                        onClick = {
                            // === ОСЬ ТУТ МАГІЯ! Витягуємо всі дані з нашої утиліти ===
                            // (Перевір, чи правильний пакет до TestAnalytics)
                            val fullCsvContent = com.mrchk.pocketdeutsch.utils.TestAnalytics.generateCsv(
                                moduleName = lesson.title,
                                userName = "Тестувальник" // Якщо в тебе на цьому екрані є справжнє ім'я користувача, підстав його сюди
                            )

                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("mashadema04@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Детальний звіт PocketDeutsch: ${lesson.title}")
                                // Вставляємо наш великий згенерований CSV у тіло листа
                                putExtra(Intent.EXTRA_TEXT, "Привіт!\nОсь мій детальний результат тестування по кожній вправі:\n\n$fullCsvContent")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    PdButton(
                        text = "2. Завантажити PDF тест",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1my4gxIdyudQt64YLz4nJQfbfSFr05N1C?usp=sharing"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    // 3. ФОРМА ВІДГУКУ
                    PdButton(
                        text = "3. Форма відгуку",
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/s3Sy47cZB9tcchndA"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )

                    Text(
                        text = "Повернутися",
                        style = PocketTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PocketTheme.colors.error,
                        modifier = Modifier
                            .clickable { showTestingMenu = false }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UnitHeader(
    unitNumber: String,
    unitTitle: String,
    unitDescription: String,
    completedLessons: Int,
    totalLessons: Int,
    progress: Float
) {
    val ink: Color = PocketTheme.colors.ink

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.surface)
            .drawBehind {
                drawLine(
                    color = ink,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column (
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = unitNumber.uppercase(),
                    style = PocketTheme.typography.labelSmall,
                    color = PocketTheme.colors.gray500,
                    letterSpacing = 1.sp
                )
                Text(
                    text = unitTitle,
                    style = PocketTheme.typography.headlineLarge,
                    color = PocketTheme.colors.ink
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .background(PocketTheme.colors.warning, RoundedCornerShape(8.dp))
                    .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$completedLessons / $totalLessons",
                    style = PocketTheme.typography.labelMedium,
                    color = PocketTheme.colors.ink
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = unitDescription,
            style = PocketTheme.typography.bodyMedium,
            color = PocketTheme.colors.gray500
        )

        Spacer(modifier = Modifier.height(16.dp))

        PdProgressBar(
            progress = progress,
            size = PdProgressSize.Small,
            progressColor = PocketTheme.colors.success
        )
    }
}

@Composable
fun PathwayNodeItem(
    data: PathwayNodeData,
    onClick: () -> Unit
) {
    val isCompleted = data.state == NodeState.COMPLETED
    val isActive = data.state == NodeState.ACTIVE
    val isNotStarted = data.state == NodeState.NOT_STARTED

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isActive) 56.dp else 48.dp)
                .background(
                    when {
                        isActive -> PocketTheme.colors.warning
                        isCompleted -> PocketTheme.colors.success
                        else -> PocketTheme.colors.gray200
                    },
                    CircleShape
                )
                .border(2.dp, PocketTheme.colors.ink, CircleShape)
                .then(
                    if (isActive) Modifier.pdStyle(
                        shadowOffset = 2.dp,
                        cornerRadius = 100.dp,
                        backgroundColor = PocketTheme.colors.warning
                    )
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_bold),
                        contentDescription = null,
                        tint = PocketTheme.colors.ink,
                        modifier = Modifier.size(24.dp)
                    )
                }
                isActive -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star_fill), // Або інша яскрава іконка
                        contentDescription = null,
                        tint = PocketTheme.colors.ink,
                        modifier = Modifier.size(28.dp)
                    )
                }
                isNotStarted -> {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(PocketTheme.colors.gray500, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    alpha = if (isCompleted) 0.7f else 1f
                    clip = false
                }
                .pdClickable(
                    onClick = onClick,
                    cornerRadius = 16.dp,
                    backgroundColor = if (isActive) PocketTheme.colors.primary else PocketTheme.colors.surface
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = data.iconRes),
                    contentDescription = null,
                    tint = if (isActive) PocketTheme.colors.surface else PocketTheme.colors.gray500,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = data.title,
                        style = PocketTheme.typography.titleMedium,
                        color = if (isActive) PocketTheme.colors.surface else PocketTheme.colors.ink
                    )
                    Text(
                        text = data.subtitle,
                        style = PocketTheme.typography.bodySmall,
                        color = if (isActive) PocketTheme.colors.surface/*.copy(alpha = 0.9f)*/ else PocketTheme.colors.gray500
                    )
                }
            }
        }
    }
}