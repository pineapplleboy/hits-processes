package com.example.googleclass.feature.courses.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import kotlinx.coroutines.launch

class ArchivedCoursesViewModel(
    private val coursesApi: CoursesApi,
) : ViewModel() {

    var state by mutableStateOf<ArchivedCoursesScreenState>(ArchivedCoursesScreenState.Loading)
        private set

    init {
        loadArchivedCourses()
    }

    fun refresh() {
        loadArchivedCourses()
    }

    private fun loadArchivedCourses() {
        state = ArchivedCoursesScreenState.Loading
        viewModelScope.launch {
            try {
                val response = coursesApi.getMyCourses(isArchived = true)
                if (!response.isSuccessful) {
                    state = ArchivedCoursesScreenState.Error(
                        "Ошибка загрузки: ${response.code()}"
                    )
                    return@launch
                }
                val courses = response.body().orEmpty().map { dto ->
                    CourseUiItem(
                        id = dto.id,
                        name = dto.name,
                        subject = dto.description ?: "",
                        role = mapRole(dto.currentUserCourseRole),
                    )
                }
                state = ArchivedCoursesScreenState.Content(courses = courses)
                Log.d(TAG, "loadArchivedCourses: success, size = ${courses.size}")
            } catch (e: Exception) {
                Log.e(TAG, "loadArchivedCourses: exception", e)
                state = ArchivedCoursesScreenState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    companion object {
        private const val TAG = "ArchivedCourses"

        private fun mapRole(apiRole: String?): String = when (apiRole) {
            "HEAD_TEACHER" -> "Главный преподаватель"
            "TEACHER" -> "Преподаватель"
            "STUDENT" -> "Студент"
            else -> ""
        }
    }
}

sealed interface ArchivedCoursesScreenState {
    data object Loading : ArchivedCoursesScreenState
    data class Content(val courses: List<CourseUiItem>) : ArchivedCoursesScreenState
    data class Error(val message: String) : ArchivedCoursesScreenState
}
