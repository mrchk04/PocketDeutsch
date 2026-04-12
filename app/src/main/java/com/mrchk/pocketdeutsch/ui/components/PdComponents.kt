package com.mrchk.pocketdeutsch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrchk.pocketdeutsch.R

import com.mrchk.pocketdeutsch.ui.theme.*

@Composable
fun ShowcaseScreen() {
    // Стейт для нижнього меню
    var selectedBottomTab by remember { mutableStateOf(0) }

    // Дані для нижнього меню
    val bottomNavItems = listOf(
        BottomNavItem("Головна", R.drawable.ic_house_bold),
        BottomNavItem("Навчання", R.drawable.ic_graduation_cap_bold),
        BottomNavItem("Прогрес", R.drawable.ic_trend_up_bold),
        BottomNavItem("Профіль", R.drawable.ic_user_bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper) // Тло всього екрану
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. Top Bars ---
        Text(
            text = "Top Bars",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        PdHomeTopBar(
            userName = "Mariia",
            onProfileClick = {},
        )
        PdTitleTopBar(
            title = "Заголовок",
            onBackClick = {},
            onRightButtonClick = {},
        )
        Spacer(modifier = Modifier.height(16.dp))
        PdExerciseTopBar(
            progress = 0.3f,
            progressText = "4/12",
            onBackClick = {}
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Основний контент із відступами
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // --- 2. Course Card ---
            Text(
                text = "Course Card",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            PdCourseCard(
                levelText = "B1.2",
                label = "Остання тема",
                title = "Lesson",
                progress = 0.6f,
                buttonText = "Button",
                onButtonClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. Tool Cards ---
            Text(
                text = "Tool Cards",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PdToolCard(
                    title = "Флешкартки",
                    icon = Icons.Outlined.Email,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                PdToolCard(
                    title = "Граматика",
                    icon = Icons.Outlined.Edit,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PdToolCard(
                    title = "Словник",
                    icon = Icons.Outlined.List,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                PdToolCard(
                    title = "Всі книги",
                    icon = Icons.Outlined.Favorite,
                    isDashed = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. Buttons ---
            Text(
                text = "Buttons",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PdButton(
                    text = "Button",
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                PdButton(
                    text = "Button",
                    onClick = { },
                    isSecondary = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка з іншим кольором (наприклад, Error)
            PdButton(
                text = "Delete",
                onClick = { },
                backgroundColor = Error,
                iconRes = R.drawable.ic_x_bold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Content Card",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        PdContentCard {
            Text(
                text = "Правило",
                style = PocketTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Більшість дієслів мають закінчення -en. Щоб змінити дієслово...")
        }
        Spacer(modifier = Modifier.height(46.dp))
        Text(
            text = "Sticky Note",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        PdStickyNote {
            // Заголовок з іконкою
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exclamation_mark_bold), // Твоя іконка
                    contentDescription = null,
                    tint = Color(0xFFF97316), // Помаранчевий
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Achtung!",
                    style = PocketTheme.typography.titleLarge,
                    color = PocketTheme.colors.ink // Чорний текст, а не рожевий!
                )
            }
            PdCallout(
                lineColor = Color(0xFFF97316), // Помаранчева лінія
                backgroundColor = Color.White.copy(alpha = 0.5f) // Напівпрозорий білий фон
            ) {
                Text(
                    text = "arbeiten -> er arbeitet",
                    style = PocketTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Якщо основа дієслова закінчується на -t або -d, ми додаємо додаткову 'e' перед закінченням.",
                style = PocketTheme.typography.bodyLarge
            )

            // (Тут далі можеш вставити PdCallout з прикладом arbeiten)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Example Items",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        PdContentCard(backgroundColor = PocketTheme.colors.tertiary) {
            Text(
                text = "Приклади",
                style = PocketTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            PdExampleItem(
                germanText = "Ich *lerne* Deutsch.",
                ukrainianText = "Я вчу німецьку."
            )
            Spacer(modifier = Modifier.height(8.dp))
            PdExampleItem(
                germanText = "Du trinkst Kaffee.",
                ukrainianText = "Ти п'єш каву."
            )
        }

        Spacer(modifier = Modifier.height(46.dp))
        Text(
            text = "Checkbox + RadioButton",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        var isChecked1 by remember { mutableStateOf(false) }
        var isChecked2 by remember { mutableStateOf(true) }

        Column(modifier = Modifier.padding(16.dp)) {
            // Просто "голий" чекбокс
            PdCheckbox(
                checked = isChecked1,
                onCheckedChange = { isChecked1 = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Чекбокс із текстом (наш LabeledRow)
            PdCheckboxRow(
                text = "Я погоджуюсь з умовами",
                checked = isChecked2,
                onCheckedChange = { isChecked2 = it }
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            var selectedOption by remember { mutableStateOf("Option 1") }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PdRadioButton(
                    selected = selectedOption == "Option 1",
                    onClick = { selectedOption = "Option 1" }
                )

                PdRadioButton(
                    selected = selectedOption == "Option 2",
                    onClick = { selectedOption = "Option 2" }
                )

                PdRadioButton(
                    selected = selectedOption == "Option 3",
                    onClick = { selectedOption = "Option 3" }
                )
            }

            var selectedId by remember { mutableStateOf(1) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Виберіть рівень складності:",
                    style = PocketTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PdRadioButtonRow(
                    text = "Легкий (A1)",
                    selected = selectedId == 1,
                    onClick = { selectedId = 1 }
                )

                PdRadioButtonRow(
                    text = "Середній (A2)",
                    selected = selectedId == 2,
                    onClick = { selectedId = 2 }
                )

                PdRadioButtonRow(
                    text = "Важкий (B1)",
                    selected = selectedId == 3,
                    onClick = { selectedId = 3 }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp)) // Відступ перед нижнім меню

        var noteText by remember { mutableStateOf("") }
        val wordCount = noteText.trim().split("\\s+".toRegex()).count { it.isNotEmpty() }

        PdPinnedCard {
            Text("Aufgabe:", style = PocketTheme.typography.titleLarge)
            Text("Deine Freundin Anna hat dich...")
        }

        Text(
            text = "SCHREIBE ÜBER DIESE PUNKTE:",
            style = PocketTheme.typography.labelSmall,
            color = PocketTheme.colors.gray500
        )
        Spacer(modifier = Modifier.height(8.dp))

        var isTask1Done by remember { mutableStateOf(false) }
        var isTask2Done by remember { mutableStateOf(true) } // Цей пункт для прикладу вже виконаний
        var isTask3Done by remember { mutableStateOf(false) }

        PdChecklistItem(
            text = "Sag Danke für die Einladung",
            isChecked = isTask1Done,
            onCheckedChange = { newValue -> isTask1Done = newValue }
            // Або коротше: { isTask1Done = it }
        )
        PdChecklistItem(
            text = "Entschuldige dich (du kommst später)",
            isChecked = isTask2Done,
            onCheckedChange = { isTask2Done = it }
        )

        PdChecklistItem(
            text = "Frag nach dem Geschenk",
            isChecked = isTask3Done,
            onCheckedChange = { isTask3Done = it }
        )


        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            PdPhraseChip(text = "Hallo Anna,", onClick = { noteText += "Hallo Anna, \n" })
            Spacer(modifier = Modifier.width(8.dp))
            PdPhraseChip(text = "Vielen Dank für...", onClick = { noteText += "Vielen Dank für " })
        }

//        PdNotepadInput(
//            value = noteText,
//            onValueChange = { noteText = it },
//            wordCount = wordCount,
//            onExpandClick = {},
//        )

        // --- 5. Bottom Bar ---
        Text(
            text = "Bottom Bar",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )
        PdBottomBar(
            items = bottomNavItems,
            selectedIndex = selectedBottomTab,
            onItemSelected = { index -> selectedBottomTab = index }
        )


    }
}