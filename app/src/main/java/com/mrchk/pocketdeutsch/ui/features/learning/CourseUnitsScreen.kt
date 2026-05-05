package com.mrchk.pocketdeutsch.ui.features.learning

import android.R.attr.translationY
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.*
import com.mrchk.pocketdeutsch.ui.theme.PocketDeutschTheme
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseUnitsScreen(
    userName: String,
    units: List<UnitData>,
    availableLevels: List<String>,
    selectedLevel: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLevelSelected: (String) -> Unit,
    onUnitClick: (String) -> Unit,
    onUnitContinue: (String) -> Unit,
    onUnitReset: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateProgress: () -> Unit,
    onNavigateProfile: () -> Unit,
) {
    // Стан для обраного таба (беремо перший рівень за замовчуванням)
//    var selectedLevel by remember(availableLevels) {
//        mutableStateOf(availableLevels.firstOrNull() ?: "")
//    }

    // Фільтруємо модулі для вибраного рівня
    val filteredUnits = remember(selectedLevel, units) {
        units.filter { it.level == selectedLevel }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = PocketTheme.colors.paper,
        topBar = {
            PdHomeTopBar(
                userName = userName,
                onProfileClick = onNavigateProfile
            )
        },
        bottomBar = {
            PdBottomBar(
                items = listOf(
                    BottomNavItem("Головна", R.drawable.ic_house_bold),
                    BottomNavItem("Навчання", R.drawable.ic_graduation_cap_bold),
                    BottomNavItem("Прогрес", R.drawable.ic_trend_up_bold),
                    BottomNavItem("Профіль", R.drawable.ic_user_bold)
                ),
                selectedIndex = 1,
                onItemSelected = { index ->
                    when (index) {
                        0 -> onNavigateHome()
                        1 -> {}
                        2 -> onNavigateProgress()
                        3 -> onNavigateProfile()
                    }
                }
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            indicator = {
                val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
                val spinAngle by infiniteTransition.animateFloat(
                    0f, 360f, infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ), "spin_angle"
                )

                // Показуємо наш кастомний UI тільки коли є свайп або йде оновлення
                if (pullToRefreshState.distanceFraction > 0f || isRefreshing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                // Плавна поява і рух вниз
                                translationY = if (isRefreshing) 32.dp.toPx() else (pullToRefreshState.distanceFraction * 32.dp.toPx())
                                alpha = if (isRefreshing) 1f else pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
                                scaleX = if (isRefreshing) 1f else pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
                                scaleY = if (isRefreshing) 1f else pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
                            }
                            .size(48.dp)
                            .background(PocketTheme.colors.primary, CircleShape)
                            // ТУТ НАШ СПРАВЖНІЙ БОРДЕР
                            .border(3.dp, PocketTheme.colors.ink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrows_clockwise_bold),
                            contentDescription = null,
                            tint = PocketTheme.colors.ink,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    // Якщо оновлюється - крутимо без зупинки. Якщо тягнемо - іконка повертається за пальцем.
                                    rotationZ = if (isRefreshing) spinAngle else pullToRefreshState.distanceFraction * 180f
                                }
                        )
                    }
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Заголовок сторінки
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Text(
                        text = "Програма курсу",
                        style = PocketTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        color = PocketTheme.colors.ink,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Пройди всі модулі, щоб розблокувати фінальний іспит.",
                        style = PocketTheme.typography.bodyMedium,
                        color = PocketTheme.colors.gray500
                    )
                }

                // Таби рівнів
                if (availableLevels.isNotEmpty()) {
                    PdLevelTabs(
                        levels = availableLevels,
                        selectedLevel = selectedLevel,
                        onLevelSelected = onLevelSelected,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (filteredUnits.isEmpty()) {
                        Text(
                            text = "Модулі для рівня $selectedLevel ще в розробці 🛠️",
                            style = PocketTheme.typography.bodyMedium,
                            color = PocketTheme.colors.gray500,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 32.dp)
                        )
                    } else {
                        filteredUnits.forEach { unit ->
                            UnitCardItem(
                                unit = unit,
                                onCardClick = { id ->
                                    onUnitClick(id)
                                },
                                onActionClick = { id ->
                                    if (unit.state == UnitState.COMPLETED) {
                                        onUnitReset(id)
                                    } else {
                                        onUnitContinue(id)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}