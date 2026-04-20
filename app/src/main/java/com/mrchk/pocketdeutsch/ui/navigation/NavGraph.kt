package com.mrchk.pocketdeutsch.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mrchk.pocketdeutsch.data.local.toUiModel
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
import com.mrchk.pocketdeutsch.ui.features.lesson.detail.CoursePathwayScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.writing.EvaluationResultScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.writing.HistoryScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.writing.HistoryViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.writing.ResultDetailViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.writing.WritingExerciseScreen
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
//        startDestination = Screen.Home.route,
//        startDestination = Screen.Writing.createRoute("test_task"),
        startDestination = Screen.LessonDetail.createRoute("les-a2-01")
    ) {
        composable(Screen.Home.route) {
            PlaceholderScreen("Main", PocketTheme.colors.primary)
            // Приклад виклику екрана написання:
            // onClick = { navController.navigate(Screen.Writing.createRoute("task_01")) }
        }

        // ... тут можуть бути інші твої екрани (Dictionary, Test, etc.) ...

        // Екран вправи
        composable(
            route = Screen.Writing.route, // "writing_screen/{exerciseId}"
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) {
            WritingExerciseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = { exerciseId ->
                    // Використовуємо нашу функцію для підстановки ID
                    navController.navigate(Screen.History.createRoute(exerciseId))
                }
            )
        }

        // Екран історії
        composable(
            route = Screen.History.route, // "history_screen/{exerciseId}"
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) {
            val historyViewModel: HistoryViewModel = hiltViewModel()

            HistoryScreen(
                viewModel = historyViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onTaskClick = { task ->
                    navController.navigate(Screen.ResultDetail.createRoute(task.timestamp))
                }
            )
        }

        composable(
            route = Screen.ResultDetail.route,
            arguments = listOf(navArgument("timestamp") { type = NavType.LongType })
        ) {
            val detailViewModel: ResultDetailViewModel = hiltViewModel()
            val taskResult by detailViewModel.taskResult.collectAsState()

            var selectedCorrection by remember { mutableStateOf<TextCorrection?>(null) }

            taskResult?.let { result ->
                EvaluationResultScreen(
                    result = result.toUiModel(), // Тут перетвори entity у модель для екрана
                    originalText = result.originalText,
                    selectedCorrection = selectedCorrection, // Або додай логіку вибору
                    onCorrectionClick = { correction ->
                        selectedCorrection = correction
                    },
                    onCloseClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Screen.LessonDetail.route, // "lesson_pathway/{lessonId}"
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) {
            CoursePathwayScreen(
                onBackClick = { navController.popBackStack() },
                onNodeClick = { nodeId ->
                    when (nodeId) {
                        "schreiben" -> navController.navigate(Screen.Writing.createRoute("les-a2-01")) // або динамічний ID
                        // інші маршрути...
                    }
                }
            )
        }

    }
}

@Composable
fun PlaceholderScreen(name: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PocketTheme.colors.paper),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = PocketTheme.typography.headlineLarge,
            color = color
        )
    }
}