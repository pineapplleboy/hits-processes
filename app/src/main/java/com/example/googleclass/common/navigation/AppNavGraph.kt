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
import com.example.googleclass.feature.course.presentation.CourseScreen
import com.example.googleclass.feature.courses.presentation.CoursesScreen
import com.example.googleclass.feature.post.presentation.PostEditorMode
import com.example.googleclass.feature.post.presentation.PostEditorScreen
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
                getAssignmentStatus = {
                    AssignmentStatusInfo(
                        AssignmentStatus.PENDING,
                        "Не сдано",
                        null,
                        100
                    )
                },
                onNavigateBack = { navController.popBackStack() },
                onAssignmentClick = { },
                onCreatePublication = { _, _, _, _ -> },
                onAddComment = { _, _ -> }
            )
        }
        composable(ScreenRoute.TaskDetail.route) {
            TaskDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStudentChat = { studentId, studentName ->
                    navController.navigate(
                        ScreenRoute.StudentChat.createRoute(studentId, studentName)
                    )
                },
            )
        }
        composable(
            route = ScreenRoute.StudentChat.route,
            arguments = listOf(
                navArgument("studentId") { defaultValue = "" },
                navArgument("studentName") { defaultValue = "" },
            ),
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val studentName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("studentName") ?: "",
                "UTF-8",
            )
            StudentChatScreen(
                studentId = studentId,
                studentName = studentName,
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
