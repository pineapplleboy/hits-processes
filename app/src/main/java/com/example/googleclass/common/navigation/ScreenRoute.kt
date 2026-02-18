package com.example.googleclass.common.navigation

sealed class ScreenRoute(val route: String) {
    data object Authorization: ScreenRoute("authorization")
}