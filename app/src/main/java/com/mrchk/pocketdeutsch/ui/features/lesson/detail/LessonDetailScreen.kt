package com.mrchk.pocketdeutsch.ui.features.lesson.detail

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mrchk.pocketdeutsch.ui.theme.PocketDeutschTheme
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

enum class NodeState { COMPLETED, ACTIVE, NOT_STARTED }

data class PathwayNodeData(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val state: NodeState
)

@Composable
fun CoursePathwayScreen(
    onBackClick: () -> Unit,
    onNodeClick: (String) -> Unit,
    viewModel: LessonDetailViewModel = hiltViewModel(),
) {

    val uiState by viewModel.uiState.collectAsState()

    Crossfade(targetState = uiState, label = "ScreenState") { state ->
        when (state) {
            is LessonDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Можна додати твій PdProgressBar або просто стандартний індикатор
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
                        val targetId = nextTask?.id ?: state.nodes.last().id
                        onNodeClick(targetId)
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
    onNodeClick: (String) -> Unit,
    onContinueClick: () -> Unit
) {
    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        containerColor = PocketTheme.colors.paper,
        topBar = {
            // Кастомний TopBar з вогником
            TopBarContainer(isDashed = false) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    PdIconButton(
                        iconRes = R.drawable.ic_arrow_left_bold, // Заміни на свою іконку
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Text(
                        text = "Рівень ${lesson.level}",
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
//                    .navigationBarsPadding()

            ) {
                PdButton(
                    text = "Продовжити Розділ",
                    onClick = onContinueClick,
                    backgroundColor = PocketTheme.colors.ink,
                    iconRes = R.drawable.ic_arrow_right_bold, // Стрілка вправо
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
            UnitHeader(
                unitTitle = lesson.topic,
                unitDescription = "Підготовка до іспиту: теорія, практика та письмове завдання.",
                progress = 0.4f
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
                            onClick = { onNodeClick(node.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitHeader(
    unitTitle: String,
    unitDescription: String,
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
                    text = "РОЗДІЛ 4",
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

            // Бейдж прогресу
            Box(
                modifier = Modifier
                    .background(PocketTheme.colors.warning, RoundedCornerShape(8.dp))
                    .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "2 / 5",
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
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isCompleted) 0.7f else 1f
                clip = false // Дозволяємо тіні виходити за межі контейнера!
            },
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


@Preview
@Composable
fun CoursePathwayScreenPreview() {
    PocketDeutschTheme {
        val mockNodes = listOf(
            PathwayNodeData("1", "Словник", "Нові слова: В аеропорту", R.drawable.ic_book_bookmark_bold, NodeState.COMPLETED),
            PathwayNodeData("2", "Читання", "Текст: Лист з відпустки", R.drawable.ic_envelope_simple_bold, NodeState.COMPLETED),
            PathwayNodeData("3", "Граматика", "Perfekt (sein vs haben)", R.drawable.ic_text_a_underline_bold, NodeState.ACTIVE),
            PathwayNodeData("4", "Аудіювання", "Діалог у готелі", R.drawable.ic_headphones_bold, NodeState.NOT_STARTED),
            PathwayNodeData("5", "Тест Розділу 4", "Перевірка знань", R.drawable.ic_graduation_cap_bold, NodeState.NOT_STARTED)
        )

        CoursePathwayScreen(
            onBackClick = {},
            onNodeClick = {},
        )
    }
}
