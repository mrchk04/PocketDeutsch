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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.domain.model.InteractiveExercise
import com.mrchk.pocketdeutsch.ui.components.PdButton
import com.mrchk.pocketdeutsch.ui.components.PdExerciseTopBar
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ListeningScreen(
    viewModel: ListeningViewModel,
    onBackClick: () -> Unit,
    onComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showTranscript by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (state.isLoading || state.practice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PocketTheme.colors.primary)
        }
        return
    }

    val listening = state.practice!!
    val currentExercise = listening.exercises.getOrNull(state.currentExerciseIndex) ?: return
    val isChecked = state.isChecked

    val totalItems = when (currentExercise) {
        is InteractiveExercise.MultipleChoice -> currentExercise.questions.size
        is InteractiveExercise.RichtigFalsch -> currentExercise.items.size
        is InteractiveExercise.Standard -> currentExercise.items.size
        else -> 0
    }
    val pagerState = rememberPagerState { totalItems }
    val currentIndex = pagerState.currentPage
    val isLastQuestion = currentIndex == totalItems - 1

    val coroutineScope = rememberCoroutineScope()

    val currentAudioUrl = listening.audioUrls.getOrNull(state.currentExerciseIndex)
    val currentTranscript = listening.transcripts.getOrNull(state.currentExerciseIndex) ?: ""

    val ink: Color = PocketTheme.colors.ink

    Scaffold(
        topBar = {
            PdExerciseTopBar(
                progress = (state.currentExerciseIndex + 1).toFloat() / listening.exercises.size,
                progressText = "${state.currentExerciseIndex + 1}/${listening.exercises.size}",
                onBackClick = onBackClick
            )
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
                    .padding(bottom = 8.dp)
            ) {
                if (isChecked) {
                    val isLastExercise = state.currentExerciseIndex == (state.practice?.exercises?.lastIndex ?: 0)

                    PdButton(
                        text = when {
                            isLastQuestion && isLastExercise -> "Завершити"
                            isLastQuestion -> "Наступна вправа"
                            else -> "Далі"
                        },
                        onClick = {
                            if (isLastQuestion) {
                                if (isLastExercise) {
                                    onComplete()
                                } else {
                                    viewModel.nextExercise()
                                    coroutineScope.launch { pagerState.scrollToPage(0) }
                                }
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(currentIndex + 1)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PdButton(
                        text = "Перевірити",
                        onClick ={
                            if (state.userAnswers.isEmpty()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Будь ласка, оберіть хоча б одну відповідь")
                                }
                            } else {
                                viewModel.checkAnswer()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
//                        enabled = state.userAnswers[currentIndex] != null
                    )
                }
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
        containerColor = PocketTheme.colors.paper
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            // Заголовок (інструкція)
            Text(
                text = currentExercise.instruction,
                style = PocketTheme.typography.titleLarge,
                color = ink,
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)
            )

            // Аудіоплеєр
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                val currentAudioUrl = listening.audioUrls.getOrNull(state.currentExerciseIndex)
                if (currentAudioUrl != null) {
                    key(currentAudioUrl) {
                        AudioPlayerCard(audioUrl = currentAudioUrl, isSeekable = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentTranscript.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = if (showTranscript) "Сховати текст" else "Показати текст аудіо",
                        style = PocketTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        color = ink,
                        modifier = Modifier
                            .clickable { showTranscript = !showTranscript }
                            .padding(vertical = 8.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showTranscript,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .background(PocketTheme.colors.surface, RoundedCornerShape(16.dp))
                            .border(2.dp, ink, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(text = currentTranscript, style = PocketTheme.typography.bodyMedium, color = ink)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { pageIndex ->
                QuestionCard(
                    exercise = currentExercise,
                    index = pageIndex,
                    selectedAnswer = state.userAnswers[pageIndex],
                    isChecked = isChecked,
                    onAnswerSelected = { answer ->
                        // Викликаємо оновлену функцію у ViewModel
                        viewModel.selectAnswerForIndex(pageIndex, answer)
                    }
                )
            }
            Row(
                Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalItems) { iteration ->
                    val color = if (pagerState.currentPage == iteration) ink else PocketTheme.colors.gray400
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    exercise: InteractiveExercise,
    index: Int,
    selectedAnswer: String?,
    isChecked: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    val ink = PocketTheme.colors.ink

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
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(2.dp, ink, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when (exercise) {
            is InteractiveExercise.RichtigFalsch -> {
                Text(
                    text = exercise.items[index],
                    style = PocketTheme.typography.titleLarge,
                    color = ink
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("richtig", "falsch").forEach { option ->
                        val correctLetter = exercise.answers.getOrNull(index) ?: ""
                        OptionButton(
                            text = option.replaceFirstChar { it.uppercase() },
                            isSelected = selectedAnswer == option,
                            isChecked = isChecked,
                            isCorrect = option.take(1).equals(correctLetter, ignoreCase = true),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onAnswerSelected(option) }
                        )
                    }
                }
            }
            is InteractiveExercise.MultipleChoice -> {
                val q = exercise.questions[index]
                Text(text = q.question, style = PocketTheme.typography.titleLarge, color = ink)
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    q.options.forEach { option ->
                        OptionButton(
                            text = option,
                            isSelected = selectedAnswer == option,
                            isChecked = isChecked,
                            isCorrect = option.startsWith(q.answer, ignoreCase = true),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onAnswerSelected(option) }
                        )
                    }
                }
            }
            else -> { Text("Тип не підтримується") }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun AudioPlayerCard(audioUrl: String, isSeekable: Boolean = true) {
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
                        if (isSeekable) {
                            currentPosition = it.toLong()
                            exoPlayer.seekTo(it.toLong())
                        }
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