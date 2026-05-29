package com.mrchk.pocketdeutsch.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrchk.pocketdeutsch.R
import com.mrchk.pocketdeutsch.ui.components.BottomNavItem
import com.mrchk.pocketdeutsch.ui.components.PdBottomBar
import com.mrchk.pocketdeutsch.ui.components.PdCourseCard
import com.mrchk.pocketdeutsch.ui.components.PdHomeTopBar
import com.mrchk.pocketdeutsch.ui.components.PdToolCard
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {},
    onCourseClick: () -> Unit = {},
    onFlashcardsClick: () -> Unit = {},
    onAllBooksClick: () -> Unit = {},
    onNavigateProgress: () -> Unit = {}
) {
    // Збираємо стани з ViewModel
    val userName by viewModel.userName.collectAsState()
    val wordOfDay by viewModel.wordOfDay.collectAsState()
    val courseProgress by viewModel.courseProgress.collectAsState()

    Scaffold(
        topBar = {
            PdHomeTopBar(
                userName = userName,
                onProfileClick = onProfileClick
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
                    selectedIndex = 0,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> {}
                            1 -> onCourseClick()
                            2 -> onNavigateProgress()
                            3 -> onProfileClick()
                        }
                    }
                )
            },
        containerColor = PocketTheme.colors.paper
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Відступи від Scaffold (щоб контент не залазив під TopBar)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. Великий заголовок ---
            Text(
                text = "Wie geht's dir?",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PocketTheme.colors.ink
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. Слово дня ---
            Text(
                text = "Слово дня",
                style = PocketTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PdWordOfDayCard(
                word = wordOfDay.word,
                partOfSpeech = wordOfDay.partOfSpeech,
                translation = wordOfDay.translation,
                example = wordOfDay.example
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. Прогрес навчання ---
            Text(
                text = "Прогрес навчання",
                style = PocketTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PdCourseCard(
                levelText = courseProgress.level,
                label = "Остання тема",
                title = courseProgress.title,
                progress = courseProgress.progress,
                buttonText = "Продовжити",
                onButtonClick = onCourseClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Інструменти ---
            Text(
                text = "Інструменти",
                style = PocketTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = PocketTheme.colors.ink,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PdToolCard(
                    title = "Флешкартки",
                    iconRes = R.drawable.ic_cards_bold,
                    onClick = onFlashcardsClick,
                    modifier = Modifier.weight(1f)
                )
                PdToolCard(
                    title = "Словник",
                    iconRes = R.drawable.ic_cards_bold,
                    onClick = onAllBooksClick, // Можеш змінити на onDictionaryClick, якщо додаси в параметри
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PdToolCard(
                    title = "Граматика",
                    iconRes = R.drawable.ic_cards_bold,
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                PdToolCard(
                    title = "Всі книги",
                    iconRes = R.drawable.ic_cards_bold,
                    isDashed = true,
                    onClick = onAllBooksClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}