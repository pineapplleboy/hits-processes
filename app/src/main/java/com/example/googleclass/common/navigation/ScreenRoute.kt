package com.example.googleclass.common.navigation

sealed class ScreenRoute(val route: String) {
    data object Authorization: ScreenRoute("authorization")
    data object Courses: ScreenRoute("courses")
    data object Course: ScreenRoute("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
}