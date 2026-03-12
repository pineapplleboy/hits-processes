package com.example.googleclass.feature.course.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import android.util.Log
import com.example.googleclass.feature.course.data.mapper.toDomain
import com.example.googleclass.feature.course.data.mapper.toUserRole
import com.example.googleclass.feature.course.domain.model.Course
import com.example.googleclass.feature.course.domain.model.Publication
import com.example.googleclass.feature.course.domain.model.User
import com.example.googleclass.feature.course.domain.model.UserRole
import com.example.googleclass.feature.course.domain.repository.CourseDetailResult
import com.example.googleclass.feature.course.domain.usecase.GetCourseDetailUseCase
import com.example.googleclass.feature.courses.data.remote.CourseCreateDto
import com.example.googleclass.feature.courses.data.remote.CoursesApi
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
        val userRole: UserRole,
        val publications: List<Publication>,
        val users: Map<String, User>,
    ) : CourseDetailUiState
}

class CourseDetailViewModel(
    private val courseId: String,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val repository: com.example.googleclass.feature.course.domain.repository.CourseDetailRepository,
    private val coursesApi: CoursesApi,
    private val userApi: UserApi,
) : ViewModel() {

    private val _uiState: MutableStateFlow<CourseDetailUiState> =
        MutableStateFlow(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
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

                val currentUser: User = profile?.toDomain()
                    ?: User(id = "", name = "", email = "")

                val role: UserRole = data.course.currentUserRole
                    ?: run {
                        // Fallback: infer from participants by profile id if present
                        if (profile != null) {
                            data.course.participants
                                .firstOrNull { it.userId == profile.id }
                                ?.role
                        } else null
                    }
                    ?: UserRole.STUDENT

                val isTeacher = role == UserRole.MAIN_TEACHER || role == UserRole.TEACHER
                val isMainTeacher = role == UserRole.MAIN_TEACHER

                _uiState.value = CourseDetailUiState.Content(
                    course = data.course,
                    currentUser = currentUser,
                    isTeacher = isTeacher,
                    isMainTeacher = isMainTeacher,
                    userRole = role,
                    publications = data.publications,
                    users = data.users,
                )
            } catch (e: Exception) {
                _uiState.value = CourseDetailUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun onPromoteClick(userId: String, currentRole: UserRole) {
        val state = _uiState.value
        if (state !is CourseDetailUiState.Content) return

        val targetRole = when (currentRole) {
            UserRole.STUDENT -> UserRole.TEACHER
            UserRole.TEACHER -> if (state.isMainTeacher) UserRole.MAIN_TEACHER else null
            UserRole.MAIN_TEACHER -> null
        } ?: return

        viewModelScope.launch {
            repository.changeUserRole(courseId, userId, targetRole)
                .onSuccess { refresh() }
        }
    }

    fun onDemoteClick(userId: String, currentRole: UserRole) {
        val state = _uiState.value
        if (state !is CourseDetailUiState.Content) return

        viewModelScope.launch {
            when (currentRole) {
                UserRole.STUDENT -> {
                    // Преподаватель может удалять студентов
                    repository.removeUserFromCourse(courseId, userId)
                        .onSuccess { refresh() }
                }

                UserRole.TEACHER -> {
                    // Главный преподаватель может понизить преподавателя до студента
                    if (state.isMainTeacher) {
                        repository.changeUserRole(courseId, userId, UserRole.STUDENT)
                            .onSuccess { refresh() }
                    }
                }

                UserRole.MAIN_TEACHER -> {
                    // Ничего не делаем
                }
            }
        }
    }

    fun updateCourse(name: String, description: String) {
        if (name.length < 3 || description.length < 3) {
            Log.d("CourseDetailViewModel", "updateCourse: validation failed")
            return
        }
        viewModelScope.launch {
            try {
                val response = coursesApi.updateCourse(
                    courseId = courseId,
                    body = CourseCreateDto(
                        name = name,
                        description = description,
                    ),
                )
                if (response.isSuccessful) {
                    Log.d("CourseDetailViewModel", "updateCourse: success")
                    refresh()
                } else {
                    Log.d("CourseDetailViewModel", "updateCourse: error ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("CourseDetailViewModel", "updateCourse: exception", e)
            }
        }
    }

    fun leaveCourse() {
        viewModelScope.launch {
            try {
                val response = coursesApi.leaveCourse(courseId)
                if (response.isSuccessful) {
                    Log.d("CourseDetailViewModel", "leaveCourse: success")
                    _uiState.value = CourseDetailUiState.Error("Вы вышли из курса")
                } else {
                    Log.d("CourseDetailViewModel", "leaveCourse: error ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("CourseDetailViewModel", "leaveCourse: exception", e)
            }
        }
    }

    fun toggleArchive() {
        val state = _uiState.value
        if (state !is CourseDetailUiState.Content) return

        val targetArchived = !state.course.isArchived
        viewModelScope.launch {
            try {
                val response = coursesApi.setCourseArchived(
                    courseId = courseId,
                    isArchived = targetArchived,
                )
                if (response.isSuccessful) {
                    Log.d("CourseDetailViewModel", "toggleArchive: success -> $targetArchived")
                    refresh()
                } else {
                    Log.d("CourseDetailViewModel", "toggleArchive: error ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("CourseDetailViewModel", "toggleArchive: exception", e)
            }
        }
    }
}

