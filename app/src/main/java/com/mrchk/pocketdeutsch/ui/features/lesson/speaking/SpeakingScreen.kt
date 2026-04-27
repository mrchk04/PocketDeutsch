package com.mrchk.pocketdeutsch.ui.features.lesson.speaking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdIconButton
import com.mrchk.pocketdeutsch.ui.components.PdTitleTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun SpeakingScreen(
    viewModel: SpeakingViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit
) {
    val data by viewModel.speakingData.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()

    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val speaking = data!!

    var activeHint by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            PdTitleTopBar(
                title = speaking.taskType.replace("_", " ").uppercase(),
                onBackClick = onBackClick,
                onRightButtonClick = { activeHint = "tips" },
                rightButtonIcon = R.drawable.ic_lightbulb_bold,
                rightButtonIconColor = PocketTheme.colors.warning,
            )
        },
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TaskContainer(
                    taskType = speaking.taskType,
                    prompt = speaking.prompt,
                    subPrompt = "Підготуйте монолог на основі матеріалів нижче."
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопки підказок
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HintButton(
                        text = "Опора",
                        icon = R.drawable.ic_check_bold, // Змінено на Int ресурс
                        backgroundColor = PocketTheme.colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = { activeHint = if (activeHint == "context") null else "context" }
                    )

                    HintButton(
                        text = "Фрази",
                        icon = R.drawable.ic_bookmark_bold, // Передаємо лише Int, без painterResource
                        backgroundColor = PocketTheme.colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { activeHint = if (activeHint == "phrases") null else "phrases" }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- ЗОНА ВІДОБРАЖЕННЯ ПІДКАЗОК ---
                AnimatedVisibility(visible = activeHint == "tips" && speaking.examTips.isNotEmpty()) {
                    ContentCard(title = "Поради до іспиту", items = speaking.examTips)
                }

                AnimatedVisibility(visible = activeHint == "context") {
                    ContentCard(title = "Опора для відповіді", text = speaking.imageDescription)
                }

                AnimatedVisibility(visible = activeHint == "phrases" && speaking.usefulPhrases.isNotEmpty()) {
                    ContentCard(title = "Redemittel", items = speaking.usefulPhrases)
                }

                // Кнопка для прикладу відповіді
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Показати приклад відповіді",
                    style = PocketTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PocketTheme.colors.primary,
                    modifier = Modifier
                        .clickable { activeHint = if (activeHint == "example") null else "example" }
                        .padding(8.dp)
                )

                AnimatedVisibility(visible = activeHint == "example") {
                    ContentCard(title = "Приклад відповіді", text = speaking.exampleResponse)
                }
            }

            // --- ТЕМНА ЗОНА ПІДГОТОВКИ (Bottom Area) ---
            PrepZone(
                timeString = viewModel.formatTime(timeLeft),
                onComplete = onComplete
            )
        }
    }
}

@Composable
fun TaskContainer(taskType: String, prompt: String, subPrompt: String) {
    val displayType = when (taskType) {
        "describe_image" -> "Опис зображення"
        "presentation" -> "Презентація"
        "role_play" -> "Діалог"
        else -> "Вільна відповідь"
    }
    val ink: Color = PocketTheme.colors.ink

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            }
            .background(PocketTheme.colors.surface, RoundedCornerShape(20.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .background(PocketTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(50))
                .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayType,
                style = PocketTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PocketTheme.colors.ink
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = prompt,
            style = PocketTheme.typography.titleLarge,
            color = PocketTheme.colors.ink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = subPrompt,
            style = PocketTheme.typography.bodyMedium,
            color = PocketTheme.colors.gray500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HintButton(
    text: String,
    icon: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val ink: Color = PocketTheme.colors.ink
    Row(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = null, tint = PocketTheme.colors.ink, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = PocketTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PocketTheme.colors.ink)
    }
}

@Composable
fun ContentCard(title: String, text: String? = null, items: List<String>? = null) {
    val ink: Color = PocketTheme.colors.ink

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(text = title, style = PocketTheme.typography.titleMedium, color = PocketTheme.colors.ink)
        Spacer(modifier = Modifier.height(12.dp))

        if (text != null) {
            Text(text = text, style = PocketTheme.typography.bodyMedium, color = PocketTheme.colors.ink, lineHeight = 24.sp)
        }

        if (items != null) {
            items.forEach { item ->
                Row(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(text = "•", modifier = Modifier.width(16.dp), color = PocketTheme.colors.ink)
                    Text(text = item, style = PocketTheme.typography.bodyMedium, color = PocketTheme.colors.ink)
                }
            }
        }
    }
}

// Темна зона підготовки
@Composable
fun PrepZone(timeString: String, onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PocketTheme.colors.ink, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .padding(top = 32.dp, bottom = 40.dp, start = 24.dp, end = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Декоративний статус
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(PocketTheme.colors.warning, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ЧАС НА ПІДГОТОВКУ",
                style = PocketTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = PocketTheme.colors.warning
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Великий таймер
        Text(
            text = timeString,
            style = PocketTheme.typography.headlineMedium.copy(fontSize = 48.sp, fontWeight = FontWeight.Black),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка
        PdButton(
            text = "Я готовий!",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = PocketTheme.colors.success
        )
    }
}