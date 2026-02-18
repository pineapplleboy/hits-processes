package com.example.googleclass.common.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.googleclass.common.navigation.AppNavGraph
import com.example.googleclass.common.presentation.theme.GoogleClassTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleClassTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}
