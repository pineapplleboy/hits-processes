package com.example.googleclass.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.googleclass.feature.authorization.presentation.AuthorizationScreen
import com.example.googleclass.feature.course.domain.model.AssignmentStatus
import com.example.googleclass.feature.course.domain.model.AssignmentStatusInfo
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.CourseParticipant
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.course.presentation.CourseScreenRoute
import com.example.googleclass.feature.courses.presentation.CoursesScreen
import com.example.googleclass.feature.post.presentation.PostEditorMode
import com.example.googleclass.feature.post.presentation.PostEditorScreen
import com.example.googleclass.feature.profile.presentation.ProfileScreen
import com.example.googleclass.feature.taskdetail.studentchat.presentation.StudentChatScreen
import com.example.googleclass.feature.taskdetail.presentation.TaskDetailScreen
import androidx.navigation.NavType

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(ScreenRoute.Authorization.route) {
            AuthorizationScreen(
                onAuthSuccess = {
                    navController.navigate(ScreenRoute.Courses.route) {
                        popUpTo(ScreenRoute.Authorization.route) { inclusive = true }
                    }
                }
            )
        }
        composable(ScreenRoute.Courses.route) {
            CoursesScreen(
                onCourseClick = { courseId ->
                    navController.navigate(ScreenRoute.Course.createRoute(courseId))
                },
                onTaskClick = { },
                onLogoutClick = {
                    navController.navigate(ScreenRoute.Authorization.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onProfileClick = {
                    navController.navigate(ScreenRoute.Profile.route)
                },
            )
        }
        composable(
            route = ScreenRoute.Course.route,
            arguments = listOf(navArgument("courseId") { defaultValue = "" })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            CourseScreenRoute(
                courseId = courseId,
                onNavigateBack = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate(ScreenRoute.PostEditor.createRoute(courseId, postId))
                },
                onAssignmentClick = { postId, userRole ->
                    navController.navigate(ScreenRoute.TaskDetail.createRoute(courseId, postId, userRole))
                },
                onCreatePublicationClick = {
                    navController.navigate(ScreenRoute.PostEditor.createRoute(courseId))
                },
            )
        }
        composable(ScreenRoute.TaskDetail.route) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val userRoleName = backStackEntry.arguments?.getString("userRole") ?: UserRole.STUDENT.name
            val userRole = runCatching { UserRole.valueOf(userRoleName) }.getOrElse { UserRole.STUDENT }

            TaskDetailScreen(
                courseId = courseId,
                postId = postId,
                userRole = userRole,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { cId, pId ->
                    navController.navigate(ScreenRoute.PostEditor.createRoute(cId, pId))
                },
                onNavigateToCourseFeed = { cId ->
                    navController.navigate(ScreenRoute.Course.createRoute(cId)) {
                        popUpTo(ScreenRoute.Courses.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToStudentChat = { taskAnswerId, studentName, studentUserId, currentUserId ->
                    navController.navigate(
                        ScreenRoute.StudentChat.createRoute(taskAnswerId, studentName, studentUserId, currentUserId)
                    )
                },
            )
        }
        composable(
            route = ScreenRoute.StudentChat.route,
            arguments = listOf(
                navArgument("taskAnswerId") { defaultValue = "" },
                navArgument("studentName") { defaultValue = "" },
                navArgument("studentUserId") { defaultValue = "" },
                navArgument("currentUserId") { defaultValue = "" },
            ),
        ) { backStackEntry ->
            val taskAnswerId = backStackEntry.arguments?.getString("taskAnswerId") ?: ""
            val studentName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("studentName") ?: "",
                "UTF-8",
            )
            val studentUserId = backStackEntry.arguments?.getString("studentUserId") ?: ""
            val currentUserId = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("currentUserId") ?: "",
                "UTF-8",
            )
            StudentChatScreen(
                taskAnswerId = taskAnswerId,
                studentName = studentName,
                studentUserId = studentUserId,
                currentUserId = currentUserId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = ScreenRoute.PostEditor.route,
            arguments = listOf(
                navArgument("courseId") { defaultValue = "" },
                navArgument("postId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
            ),
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            val postId = backStackEntry.arguments?.getString("postId")
                ?.takeIf { it.isNotEmpty() }

            val mode = if (postId != null) {
                PostEditorMode.Edit(courseId = courseId, postId = postId)
            } else {
                PostEditorMode.Create(courseId = courseId)
            }

            PostEditorScreen(
                mode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCourseFeed = { cId ->
                    val route = ScreenRoute.Course.createRoute(cId)
                    if (!navController.popBackStack(route, false)) {
                        navController.navigate(route) {
                            popUpTo(ScreenRoute.Courses.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(ScreenRoute.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onArchivedCoursesClick = { },
                onSwitchUserClick = { },
                onLogoutClick = {
                    navController.navigate(ScreenRoute.Authorization.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun sampleCoursesList() = listOf(
    Course(
        id = "1",
        name = "Название курса",
        participants = listOf(
            CourseParticipant("u1", UserRole.MAIN_TEACHER),
            CourseParticipant("u2", UserRole.STUDENT),
        )
    ),
)

private fun sampleCourse(id: String) = Course(
    id = id.ifEmpty { "1" },
    name = "Название курса",
    description = null,
    joinCode = "XYZ-001",
    isArchived = false,
    participants = listOf(
        CourseParticipant("u1", UserRole.MAIN_TEACHER),
        CourseParticipant("u2", UserRole.STUDENT),
    )
)

private fun sampleCurrentUser() = User(
    id = "u1",
    name = "Преподаватель",
    email = "teacher@example.com"
)
