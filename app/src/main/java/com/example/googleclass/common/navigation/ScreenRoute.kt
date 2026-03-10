package com.example.googleclass.common.navigation

import com.example.googleclass.feature.course.domain.model.UserRole

sealed class ScreenRoute(val route: String) {
    data object Authorization: ScreenRoute("authorization")
    data object Courses: ScreenRoute("courses")
    data object Course: ScreenRoute("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    data object TaskDetail : ScreenRoute("task_detail/{taskId}/{userRole}") {
        fun createRoute(taskId: String, userRole: UserRole) =
            "task_detail/$taskId/${userRole.name}"
    }
    data object StudentChat : ScreenRoute("student_chat/{taskAnswerId}/{studentName}/{studentUserId}") {
        fun createRoute(taskAnswerId: String, studentName: String, studentUserId: String) =
            "student_chat/$taskAnswerId/${studentName.encodeForRoute()}/$studentUserId"
    }
    data object PostEditor : ScreenRoute("post_editor/{courseId}?postId={postId}") {
        fun createRoute(courseId: String, postId: String? = null): String =
            if (postId != null) "post_editor/$courseId?postId=$postId"
            else "post_editor/$courseId"
    }
    data object Profile : ScreenRoute("profile")
}

private fun String.encodeForRoute(): String =
    java.net.URLEncoder.encode(this, "UTF-8")