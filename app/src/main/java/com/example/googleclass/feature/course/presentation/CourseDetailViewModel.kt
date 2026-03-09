package com.example.googleclass.feature.course.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.course.data.mapper.toDomain
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.course.domain.repository.CourseDetailResult
import com.example.googleclass.feature.course.domain.usecase.GetCourseDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CourseDetailUiState {

    data object Loading : CourseDetailUiState

    data class Error(val message: String) : CourseDetailUiState

    data class Content(
        val course: Course,
        val currentUser: User,
        val isTeacher: Boolean,
        val isMainTeacher: Boolean,
        val publications: List<Publication>,
        val users: Map<String, User>,
    ) : CourseDetailUiState
}

class CourseDetailViewModel(
    private val courseId: String,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val userApi: UserApi,
) : ViewModel() {

    private val _uiState: MutableStateFlow<CourseDetailUiState> =
        MutableStateFlow(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.value = CourseDetailUiState.Loading
        viewModelScope.launch {
            try {
                val profileResponse = userApi.getMyProfile()
                val profile = if (profileResponse.isSuccessful) {
                    profileResponse.body()
                } else {
                    null
                }

                val detailResult = getCourseDetailUseCase(courseId)
                if (detailResult.isFailure) {
                    _uiState.value = CourseDetailUiState.Error(
                        detailResult.exceptionOrNull()?.message ?: "Не удалось загрузить курс"
                    )
                    return@launch
                }

                val data: CourseDetailResult = detailResult.getOrThrow()

                val currentUser: User
                val role: UserRole

                if (profile != null) {
                    currentUser = profile.toDomain()
                    val participantRole = data.course.participants
                        .firstOrNull { it.userId == profile.id }
                        ?.role ?: UserRole.STUDENT
                    role = participantRole
                } else {
                    currentUser = User(
                        id = "",
                        name = "",
                        email = "",
                    )
                    role = UserRole.STUDENT
                }

                val isTeacher = role == UserRole.MAIN_TEACHER || role == UserRole.TEACHER
                val isMainTeacher = role == UserRole.MAIN_TEACHER

                _uiState.value = CourseDetailUiState.Content(
                    course = data.course,
                    currentUser = currentUser,
                    isTeacher = isTeacher,
                    isMainTeacher = isMainTeacher,
                    publications = data.publications,
                    users = data.users,
                )
            } catch (e: Exception) {
                _uiState.value = CourseDetailUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}

