package com.example.googleclass.feature.course.presentation.marks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.course.data.remote.CourseDetailApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StudentMarkItem(
    val userId: String,
    val name: String,
    val email: String,
    val role: String,
)

sealed interface MarksScreenState {
    data object Loading : MarksScreenState
    data class Error(val message: String) : MarksScreenState
    data class Content(val students: List<StudentMarkItem>) : MarksScreenState
}

class MarksScreenViewModel(
    private val courseId: String,
    private val courseDetailApi: CourseDetailApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MarksScreenState>(MarksScreenState.Loading)
    val uiState: StateFlow<MarksScreenState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun refresh() {
        loadUsers()
    }

    private fun loadUsers() {
        _uiState.value = MarksScreenState.Loading
        viewModelScope.launch {
            try {
                val response = courseDetailApi.getCourseUsers(courseId)
                if (!response.isSuccessful) {
                    _uiState.value = MarksScreenState.Error("Ошибка загрузки: ${response.code()}")
                    Log.d(TAG, "getCourseUsers: error ${response.code()}")
                    return@launch
                }

                val users = response.body().orEmpty()
                val students = users
                    .filter { it.userRole == "STUDENT" }
                    .map { dto ->
                        val user = dto.userModel
                        StudentMarkItem(
                            userId = user.id,
                            name = listOfNotNull(user.lastName, user.firstName)
                                .joinToString(" ")
                                .ifBlank { user.email },
                            email = user.email,
                            role = dto.userRole,
                        )
                    }
                    .sortedBy { it.name }

                _uiState.value = MarksScreenState.Content(students = students)
                Log.d(TAG, "getCourseUsers: success, students = ${students.size}")
            } catch (e: Exception) {
                _uiState.value = MarksScreenState.Error(e.message ?: "Неизвестная ошибка")
                Log.d(TAG, "getCourseUsers: exception", e)
            }
        }
    }

    companion object {
        private const val TAG = "MarksScreen"
    }
}
