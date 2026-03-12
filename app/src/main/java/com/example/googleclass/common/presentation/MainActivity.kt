package com.example.googleclass.common.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.googleclass.common.navigation.AppNavGraph
import com.example.googleclass.common.navigation.ScreenRoute
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    private val tokenStorage: TokenStorage by inject()
    private val sessionExpiredNotifier: SessionExpiredNotifier by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        val startDestination = if (tokenStorage.getTokens() != null) {
            ScreenRoute.Courses.route
        } else {
            ScreenRoute.Authorization.route
        }
        setContent {
            GoogleClassTheme {
                val navController = rememberNavController()
                LaunchedEffect(sessionExpiredNotifier) {
                    sessionExpiredNotifier.sessionExpiredEvents.collect {
                        navController.navigate(ScreenRoute.Authorization.route) {
                            popUpTo(ScreenRoute.Courses.route) { inclusive = true }
                        }
                    }
                }
                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
