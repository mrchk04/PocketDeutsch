package com.mrchk.pocketdeutsch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mrchk.pocketdeutsch.ui.features.writing.WritingExerciseScreen
import com.mrchk.pocketdeutsch.ui.navigation.NavGraph
import com.mrchk.pocketdeutsch.ui.theme.PocketDeutschTheme
import com.mrchk.pocketdeutsch.ui.theme.PocketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketDeutschTheme {
//                ShowcaseScreen()
                val navController = rememberNavController()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PocketTheme.colors.paper)
                ) {
                    NavGraph(navController = navController)
                }
            }
        }
    }
}