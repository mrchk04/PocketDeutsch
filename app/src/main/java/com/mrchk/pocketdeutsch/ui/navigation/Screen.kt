package com.mrchk.pocketdeutsch.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Dictionary : Screen("dictionary_screen")
    object Test : Screen("test_screen")
    object Settings : Screen("settings_screen")
    object Profile : Screen("profile_screen")

    // --- Додаємо нові маршрути з параметрами ---

    object Writing : Screen("writing_screen/{exerciseId}") {
        // Функція для створення готового шляху при переході
        fun createRoute(exerciseId: String) = "writing_screen/$exerciseId"
    }

    object History : Screen("history_screen/{exerciseId}") {
        // Функція для створення готового шляху при переході
        fun createRoute(exerciseId: String) = "history_screen/$exerciseId"
    }

    object ResultDetail : Screen("result_detail/{timestamp}") {
        fun createRoute(timestamp: Long) = "result_detail/$timestamp"
    }

    object LessonDetail : Screen("lesson_detail/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson_detail/$lessonId"
    }
}