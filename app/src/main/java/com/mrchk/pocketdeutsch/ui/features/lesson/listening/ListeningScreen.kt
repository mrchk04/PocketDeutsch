package com.mrchk.pocketdeutsch.ui.features.lesson.listening

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import kotlinx.coroutines.delay

@Composable
fun ListeningScreen(
    viewModel: ListeningViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
) {
    val data by viewModel.listeningData.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()

    // Стейт для показу транскрипту
    var showTranscript by remember { mutableStateOf(false) }

    if (data == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val listening = data!!
    val exercise = listening.exercise
    if (currentIndex >= exercise.items.size) return

    val currentItem = exercise.items[currentIndex]
    val correctAnswer = exercise.answers[currentIndex]
    val isLastQuestion = currentIndex == exercise.items.size - 1

    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        topBar = {
            PdExerciseTopBar(
                progress = (currentIndex + 1).toFloat() / exercise.items.size,
                progressText = "${currentIndex + 1}/${exercise.items.size}",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PocketTheme.colors.paper)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawLine(
                            color = ink,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
            ) {
                if (isChecked) {
                    PdButton(
                        text = if (isLastQuestion) "Завершити" else "Далі",
                        onClick = {
                            if (isLastQuestion) onComplete() else viewModel.nextQuestion()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PdButton(
                        text = "Перевірити",
                        onClick = { viewModel.checkAnswer() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedAnswer != null
                    )
                }
            }
        },
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Інструкція
            Text(
                text = exercise.instruction,
                style = PocketTheme.typography.titleLarge,
                color = PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (listening.audioUrl != null) {
                AudioPlayerCard(audioUrl = listening.audioUrl)
            } else {
                Text("🎧 Аудіофайл недоступний", color = PocketTheme.colors.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // КНОПКА ТРАНСКРИПТУ
            if (listening.transcript != null) {
                Text(
                    text = if (showTranscript) "Сховати текст" else "Показати текст аудіо",
                    style = PocketTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    ),
                    color = PocketTheme.colors.ink,
                    modifier = Modifier
                        .clickable { showTranscript = !showTranscript }
                        .padding(vertical = 8.dp)
                )

                AnimatedVisibility(
                    visible = showTranscript,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) // Обмежуємо висоту, щоб не перекривало питання
                            .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
                            .border(2.dp, PocketTheme.colors.ink, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = listening.transcript,
                                style = PocketTheme.typography.bodyMedium,
                                color = PocketTheme.colors.ink,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(PocketTheme.colors.ink, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.height(20.dp))

            // ЗАПИТАННЯ ТА ВІДПОВІДІ (СКРОЛЯТЬСЯ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = currentItem,
                    style = PocketTheme.typography.titleLarge,
                    color = PocketTheme.colors.ink
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Варіанти відповідей (Richtig / Falsch)
                val options = listOf("richtig", "falsch")

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { option ->
                        OptionButton(
                            text = option.replaceFirstChar { it.uppercase() }, // Робимо з великої літери для UI
                            isSelected = selectedAnswer == option,
                            isChecked = isChecked,
                            isCorrect = option == correctAnswer,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.selectAnswer(option) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerCard(audioUrl: String) {
    val context = LocalContext.current
    val ink = PocketTheme.colors.ink

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var playCount by remember { mutableIntStateOf(0) }
    val maxPlays = 2
    val isLimitReached = playCount >= maxPlays

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration =
                        exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    playCount++
                    isPlaying = false
                    exoPlayer.seekTo(0L)
                    exoPlayer.pause()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            delay(100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )
            }
            .background(PocketTheme.colors.surface, RoundedCornerShape(24.dp))
            .border(2.dp, ink, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Аудіозапис",
                style = PocketTheme.typography.titleMedium,
                color = ink
            )

            Box(
                modifier = Modifier
                    .background(
                        if (isLimitReached) PocketTheme.colors.error else PocketTheme.colors.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, ink, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isLimitReached) "Ліміт вичерпано" else "Залишилось: ${maxPlays - playCount}",
                    style = PocketTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ink
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Кнопка Play/Pause (стає сірою і не клікабельною, якщо ліміт)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isLimitReached) PocketTheme.colors.gray200 else PocketTheme.colors.warning,
                        CircleShape
                    )
                    .border(2.dp, ink, CircleShape)
                    .clip(CircleShape)
                    .clickable(enabled = !isLimitReached) {
                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause_bold else R.drawable.ic_play_bold),
                    contentDescription = "Play/Pause",
                    tint = if (isLimitReached) PocketTheme.colors.gray500 else ink,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = {
                        // Якщо хочеш зробити ПОВНИЙ хардкор як на іспиті,
                        // закоментуй ці два рядки нижче, щоб користувач не міг перемотувати аудіо вперед-назад
                        currentPosition = it.toLong()
                        exoPlayer.seekTo(it.toLong())
                    },
                    valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isLimitReached) PocketTheme.colors.gray400 else ink,
                        activeTrackColor = if (isLimitReached) PocketTheme.colors.gray400 else PocketTheme.colors.primary,
                        inactiveTrackColor = PocketTheme.colors.gray200
                    ),
                    modifier = Modifier.height(24.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = formatTime(currentPosition), style = PocketTheme.typography.labelSmall, color = ink)
                    Text(text = formatTime(duration), style = PocketTheme.typography.labelSmall, color = ink)
                }
            }
        }
    }
}
fun formatTime(timeMs: Long): String {
    if (timeMs < 0) return "00:00"
    val totalSeconds = timeMs / 1000
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d", m, s)
}

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    isChecked: Boolean,
    isCorrect: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        isChecked && isCorrect -> PocketTheme.colors.success
        isChecked && isSelected && !isCorrect -> PocketTheme.colors.error
        !isChecked && isSelected -> Color(0xFFB8C0FF)
        else -> PocketTheme.colors.surface
    }
    val borderColor =
        if (isSelected || (isChecked && isCorrect)) PocketTheme.colors.ink else PocketTheme.colors.gray400

    val ink: Color = PocketTheme.colors.ink

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = ink,
                    topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isChecked) { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = PocketTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = PocketTheme.colors.ink
        )
    }
}