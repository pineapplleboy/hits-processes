package com.example.googleclass.common.navigation

sealed class ScreenRoute(val route: String) {
    data object Authorization: ScreenRoute("authorization")
    data object Courses: ScreenRoute("courses")
    data object Course: ScreenRoute("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    data object Authorization : ScreenRoute("authorization")
    data object TaskDetail : ScreenRoute("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
}