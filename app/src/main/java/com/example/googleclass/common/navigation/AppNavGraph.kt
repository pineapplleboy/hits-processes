package com.example.googleclass.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.googleclass.feature.authorization.presentation.AuthorizationScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Authorization.route,
    ) {
        composable(ScreenRoute.Authorization.route) {
            AuthorizationScreen()
        }
    }
}
