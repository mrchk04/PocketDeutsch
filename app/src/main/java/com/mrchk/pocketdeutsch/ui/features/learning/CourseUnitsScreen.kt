package com.mrchk.pocketdeutsch.ui.features.learning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.*
import com.mrchk.pocketdeutsch.ui.theme.PocketDeutschTheme
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun CourseUnitsScreen(
    userName: String,
    units: List<UnitData>,
    availableLevels: List<String>,
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    onUnitClick: (String) -> Unit,
    onUnitActionClick: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateProgress: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    // Стан для обраного таба (беремо перший рівень за замовчуванням)
//    var selectedLevel by remember(availableLevels) {
//        mutableStateOf(availableLevels.firstOrNull() ?: "")
//    }

    // Фільтруємо модулі для вибраного рівня
    val filteredUnits = remember(selectedLevel, units) {
        units.filter { it.level == selectedLevel }
    }

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
                    BottomNavItem("Курс", R.drawable.ic_graduation_cap_bold),
                    BottomNavItem("Прогрес", R.drawable.ic_trend_up_bold),
                    BottomNavItem("Профіль", R.drawable.ic_user_bold)
                ),
                selectedIndex = 1, // Вкладка "Курс" активна
                onItemSelected = { index ->
                    when(index) {
                        0 -> onNavigateHome()
                        1 -> {} // Ми вже тут
                        2 -> onNavigateProgress()
                        3 -> onNavigateProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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

            // Відфільтрований список карток
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
                                onUnitClick (id)
                            },
                            onActionClick = { id ->
                                onUnitActionClick(id)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}