package com.mrchk.pocketdeutsch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mrchk.pocketdeutsch.ui.features.writing.WritingExerciseScreen
import com.mrchk.pocketdeutsch.ui.theme.PocketDeutschTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketDeutschTheme {
//                ShowcaseScreen()
                WritingExerciseScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}