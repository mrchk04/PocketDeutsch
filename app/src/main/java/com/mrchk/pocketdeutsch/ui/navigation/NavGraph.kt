package com.mrchk.pocketdeutsch.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import com.mrchk.pocketdeutsch.data.mapper.toUiModel
import com.mrchk.pocketdeutsch.domain.model.TextCorrection
//import com.mrchk.pocketdeutsch.ui.features.home.HomeScreen
import com.mrchk.pocketdeutsch.ui.features.learning.CourseUnitsScreen
import com.mrchk.pocketdeutsch.ui.features.learning.CourseUnitsUiState
import com.mrchk.pocketdeutsch.ui.features.learning.CourseUnitsViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.details.CoursePathwayScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.language_use.LanguageUseScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.language_use.LanguageUseViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.listening.ListeningScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.listening.ListeningViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.reading.ReadingScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.reading.ReadingViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.speaking.SpeakingScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.speaking.SpeakingViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.theory.GrammarPracticeScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.theory.GrammarPracticeViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.theory.TheoryScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary.VocabularyPracticeScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary.VocabularyPracticeViewModel
import com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary.VocabularyScreen
import com.mrchk.pocketdeutsch.ui.features.lesson.vocabulary.VocabularyViewModel
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
//        startDestination = Screen.LessonDetail.createRoute("les-a2-01")
        startDestination = Screen.Course.route
    ) {
//        composable(Screen.Home.route) {
//            HomeScreen(
//                onCourseClick = {
//                    navController.navigate(Screen.LessonDetail.createRoute("les-a2-01"))
//                },
//                onAllBooksClick = {
//                    navController.navigate("dictionary_screen")
//                },
//                onFlashcardsClick = {
//                    navController.navigate("flashcards_screen")
//                },
//                onProfileClick = {
//                    // navController.navigate("profile_screen") // Якщо колись додаси екран профілю
//                }
//            )
//        }

        composable(
            route = Screen.Writing.route, // "writing_screen/{exerciseId}"
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) {
            WritingExerciseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHistory = { exerciseId ->
                    navController.navigate(Screen.History.createRoute(exerciseId))
                }
            )
        }

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
                    result = result.toUiModel(),
                    originalText = result.originalText,
                    selectedCorrection = selectedCorrection,
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
        ) {backStackEntry ->

            val currentLessonId = backStackEntry.arguments?.getString("lessonId") ?: ""

            CoursePathwayScreen(
                onBackClick = { navController.popBackStack() },
                onNodeClick = { id, type ->
                    when (type) {
                        "vocabulary" -> navController.navigate("vocabulary_screen/$currentLessonId")
                        "grammar" -> navController.navigate(Screen.Theory.createRoute(currentLessonId))
                        "reading" -> navController.navigate("reading_screen/$currentLessonId")
                        "listening" -> navController.navigate("listening_screen/$currentLessonId")
                        "writing" -> navController.navigate(Screen.Writing.createRoute(currentLessonId))
                        "speaking" -> navController.navigate("speaking_screen/$currentLessonId")
                        "language_use" -> navController.navigate("language_use_screen/$currentLessonId")
                    }
                }
            )
        }

        composable(Screen.Course.route) {
            val viewModel: CourseUnitsViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val selectedLevel by viewModel.selectedLevel.collectAsState()

            val isRefreshing by viewModel.isRefreshing.collectAsState()

            when (val state = uiState) {
                is CourseUnitsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PocketTheme.colors.primary)
                    }
                }
                is CourseUnitsUiState.Success -> {
                    CourseUnitsScreen(
                        userName = "Mariia",
                        units = state.units,
                        availableLevels = state.availableLevels,
                        selectedLevel = selectedLevel,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refreshData() },

                        onLevelSelected = { viewModel.selectLevel(it) },
                        onUnitClick = { unitId ->
                            navController.navigate(Screen.LessonDetail.createRoute(unitId))
                        },
                        onUnitContinue = { unitId ->
                            navController.navigate(Screen.LessonDetail.createRoute(unitId))
                        },
                        onUnitReset = { unitId ->
                            viewModel.resetUnitProgress(unitId)
                        },

                        onNavigateHome = {
//                            navController.navigate(Screen.Home.route) {
//                                popUpTo(navController.graph.startDestinationId) { saveState = true }
//                                launchSingleTop = true
//                                restoreState = true
//                            }
                        },
                        onNavigateProgress = { /* Додамо пізніше */ },
                        onNavigateProfile = { /* Додамо пізніше */ }
                    )
                }
                is CourseUnitsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = PocketTheme.colors.error)
                    }
                }
            }
        }

        composable(route = Screen.Theory.route) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""

            TheoryScreen(
                onBackClick = { navController.popBackStack() },
                onNextClick = {
                    navController.navigate("grammar_practice_screen/$lessonId")
                }
            )
        }

        composable(route = "grammar_practice_screen/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val viewModel: GrammarPracticeViewModel = hiltViewModel()

            GrammarPracticeScreen(
                onBackClick = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeGrammarNode(lessonId)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "language_use_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val viewModel = hiltViewModel<LanguageUseViewModel>()

            LanguageUseScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeExerciseNode(lessonId)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "speaking_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            val viewModel = hiltViewModel<SpeakingViewModel>()

            SpeakingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeExerciseNode()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "reading_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel = hiltViewModel<ReadingViewModel>()
            ReadingScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeExerciseNode()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "listening_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) {
            val viewModel = hiltViewModel<ListeningViewModel>()
            ListeningScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onComplete = {
                    viewModel.completeExerciseNode()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "vocabulary_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: VocabularyViewModel = hiltViewModel()
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""

            VocabularyScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onNextClick = {
                    navController.navigate("vocabulary_practice_screen/$lessonId")
                }
            )
        }

        composable(
            route = "vocabulary_practice_screen/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Ініціалізуємо нашу нову ViewModel
            val viewModel: VocabularyPracticeViewModel = hiltViewModel()
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""

            VocabularyPracticeScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onComplete = {
                    viewModel.completeVocabularyNode(lessonId)
                    navController.popBackStack("vocabulary_screen/$lessonId", inclusive = false)
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