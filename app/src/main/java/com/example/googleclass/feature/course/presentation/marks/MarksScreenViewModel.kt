package com.example.googleclass.feature.course.presentation.marks

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.course.data.remote.CourseDetailApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                val courseResponse = courseDetailApi.getCourse(courseId)
                val isPassFail = courseResponse.body()?.courseMarkEvaluationType == "PASS_FAIL"

                val response = courseDetailApi.getCourseUsers(courseId)
                if (!response.isSuccessful) {
                    _uiState.value = MarksScreenState.Error("${response.code()}")
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
                            score = dto.score,
                            isPassFail = isPassFail,
                        )
                    }
                    .sortedBy { it.name }

                _uiState.value = MarksScreenState.Content(students = students)
                Log.d(TAG, "getCourseUsers: success, students = ${students.size}")
            } catch (e: Exception) {
                _uiState.value = MarksScreenState.Error(e.message ?: "")
                Log.d(TAG, "getCourseUsers: exception", e)
            }
        }
    }

    companion object {
        private const val TAG = "MarksScreen"
    }
}
