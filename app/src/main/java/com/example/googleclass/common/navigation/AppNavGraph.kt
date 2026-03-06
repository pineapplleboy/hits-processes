package com.example.googleclass.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.googleclass.feature.authorization.presentation.AuthorizationScreen

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
                courses = sampleCoursesList(),
                onCourseClick = { courseId ->
                    navController.navigate(ScreenRoute.Course.createRoute(courseId))
                }
            )
        }
        composable(
            route = ScreenRoute.Course.route,
            arguments = listOf(navArgument("courseId") { defaultValue = "" })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            CourseScreen(
                course = sampleCourse(courseId),
                currentUser = sampleCurrentUser(),
                isTeacher = true,
                publications = emptyList(),
                submissions = emptyList(),
                users = emptyMap(),
                getAssignmentStatus = { AssignmentStatusInfo(AssignmentStatus.PENDING, "Не сдано", null, 100) },
                onNavigateBack = { navController.popBackStack() },
                onAssignmentClick = { },
                onCreatePublication = { _, _, _, _ -> },
                onAddComment = { _, _ -> }
            )
        }
        composable(ScreenRoute.TaskDetail.route) {
            TaskDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

private fun sampleCoursesList() = listOf(
    Course("1", "Название курса", listOf(CourseParticipant("u1", UserRole.MAIN_TEACHER), CourseParticipant("u2", UserRole.STUDENT))),
)

private fun sampleCourse(id: String) = Course(
    id = id.ifEmpty { "1" },
    name = "Название курса",
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
