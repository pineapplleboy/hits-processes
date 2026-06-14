package com.example.googleclass.common.navigation

import com.example.googleclass.feature.course.domain.model.UserRole

sealed class ScreenRoute(val route: String) {
    data object Authorization: ScreenRoute("authorization")
    data object Courses: ScreenRoute("courses")
    data object Course: ScreenRoute("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    data object TaskDetail : ScreenRoute("task_detail/{courseId}/{postId}/{userRole}") {
        fun createRoute(courseId: String, postId: String, userRole: UserRole) =
            "task_detail/$courseId/$postId/${userRole.name}"
    }
    data object StudentChat : ScreenRoute("student_chat/{taskAnswerId}/{studentName}/{studentUserId}/{currentUserId}") {
        fun createRoute(taskAnswerId: String, studentName: String, studentUserId: String, currentUserId: String = "") =
            "student_chat/$taskAnswerId/${studentName.encodeForRoute()}/$studentUserId/${currentUserId.encodeForRoute()}"
    }
    data object PostEditor : ScreenRoute("post_editor/{courseId}?postId={postId}") {
        fun createRoute(courseId: String, postId: String? = null): String =
            if (postId != null) "post_editor/$courseId?postId=$postId"
            else "post_editor/$courseId"
    }
    data object Criteria : ScreenRoute("criteria/{courseId}/{postId}") {
        fun createRoute(courseId: String, postId: String) = "criteria/$courseId/$postId"
    }
    data object CriteriaEvaluation : ScreenRoute("criteria_evaluation/{courseId}/{postId}/{taskAnswerId}") {
        fun createRoute(courseId: String, postId: String, taskAnswerId: String) =
            "criteria_evaluation/$courseId/$postId/$taskAnswerId"
    }
    data object PeerReviewList : ScreenRoute("peer_review/{courseId}/{postId}") {
        fun createRoute(courseId: String, postId: String) = "peer_review/$courseId/$postId"
    }
    data object Profile : ScreenRoute("profile")
    data object ArchivedCourses : ScreenRoute("archived_courses")
    data object Marks : ScreenRoute("marks/{courseId}") {
        fun createRoute(courseId: String) = "marks/$courseId"
    }
}

private fun String.encodeForRoute(): String =
    java.net.URLEncoder.encode(this, "UTF-8")
