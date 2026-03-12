package com.example.googleclass.feature.courses.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.authorization.domain.repository.AuthRepository
import com.example.googleclass.feature.taskdetail.presentation.TaskDetailViewModel
import com.example.googleclass.feature.courses.data.remote.CourseCreateDto
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import com.example.googleclass.feature.courses.data.remote.TaskAnswerApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CoursesScreenViewModel(
    private val coursesApi: CoursesApi,
    private val userApi: UserApi,
    private val authRepository: AuthRepository,
    private val taskAnswerApi: TaskAnswerApi,
) : ViewModel() {

    var state by mutableStateOf<CoursesScreenState>(CoursesScreenState.Loading)
        private set

    var dialogState by mutableStateOf<CreateCourseDialogState?>(null)
        private set

    var joinDialogState by mutableStateOf<JoinCourseDialogState?>(null)
        private set

    /** true после успешного logout — сигнал для навигации */
    var logoutCompleted by mutableStateOf(false)
        private set

    init {
        loadData()
    }

    // region Logout

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                Log.e(TAG, "logout: exception", e)
            } finally {
                logoutCompleted = true
            }
        }
    }

    // endregion

    // region Create Course Dialog

    fun openCreateCourseDialog() {
        dialogState = CreateCourseDialogState()
    }

    fun dismissCreateCourseDialog() {
        if (dialogState?.isCreating == true) return
        dialogState = null
    }

    fun onCourseNameChanged(value: String) {
        dialogState = dialogState?.copy(name = value, error = null)
    }

    fun onCourseDescriptionChanged(value: String) {
        dialogState = dialogState?.copy(description = value, error = null)
    }

    fun submitCreateCourse() {
        val dialog = dialogState ?: return
        if (dialog.isCreating) return

        val name = dialog.name.trim()
        val description = dialog.description.trim()

        if (name.length < 3) {
            dialogState = dialog.copy(error = "Название должно содержать минимум 3 символа")
            return
        }
        if (description.length < 3) {
            dialogState = dialog.copy(error = "Описание должно содержать минимум 3 символа")
            return
        }

        dialogState = dialog.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                val response = coursesApi.createCourse(
                    CourseCreateDto(name = name, description = description)
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "createCourse: success")
                    dialogState = null
                    loadData()
                } else {
                    Log.d(TAG, "createCourse: error ${response.code()}")
                    dialogState = dialogState?.copy(
                        isCreating = false,
                        error = "Ошибка создания курса: ${response.code()}",
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "createCourse: exception", e)
                dialogState = dialogState?.copy(
                    isCreating = false,
                    error = e.message ?: "Неизвестная ошибка",
                )
            }
        }
    }

    // endregion

    // region Join Course Dialog

    fun openJoinCourseDialog() {
        joinDialogState = JoinCourseDialogState()
    }

    fun dismissJoinCourseDialog() {
        if (joinDialogState?.isJoining == true) return
        joinDialogState = null
    }

    fun onJoinCodeChanged(value: String) {
        joinDialogState = joinDialogState?.copy(code = value, error = null)
    }

    fun submitJoinCourse() {
        val dialog = joinDialogState ?: return
        if (dialog.isJoining) return

        val code = dialog.code.trim()
        if (code.isEmpty()) {
            joinDialogState = dialog.copy(error = "Введите код курса")
            return
        }

        joinDialogState = dialog.copy(isJoining = true, error = null)

        viewModelScope.launch {
            try {
                val response = coursesApi.joinCourseByCode(code)
                if (response.isSuccessful) {
                    Log.d(TAG, "joinCourse: success")
                    joinDialogState = null
                    loadData()
                } else {
                    Log.d(TAG, "joinCourse: error ${response.code()}")
                    joinDialogState = joinDialogState?.copy(
                        isJoining = false,
                        error = if (response.code() == 404) "Курс с таким кодом не найден"
                                else "Ошибка: ${response.code()}",
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "joinCourse: exception", e)
                joinDialogState = joinDialogState?.copy(
                    isJoining = false,
                    error = e.message ?: "Неизвестная ошибка",
                )
            }
        }
    }

    // endregion

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        state = CoursesScreenState.Loading
        viewModelScope.launch {
            try {
                val profileDeferred = async { userApi.getMyProfile() }
                val coursesDeferred = async { coursesApi.getMyCourses(isArchived = false) }
                val tasksDeferred = async { taskAnswerApi.getAllUserTaskAnswers() }

                val profileResponse = profileDeferred.await()
                val coursesResponse = coursesDeferred.await()
                val tasksResponse = tasksDeferred.await()

                if (!coursesResponse.isSuccessful) {
                    state = CoursesScreenState.Error("Ошибка загрузки курсов: ${coursesResponse.code()}")
                    Log.d(TAG, "getMyCourses: error code = ${coursesResponse.code()}")
                    return@launch
                }

                val courseDtos = coursesResponse.body().orEmpty()
                val courses = courseDtos.map { dto ->
                    CourseUiItem(
                        id = dto.id,
                        name = dto.name,
                        subject = dto.description ?: "",
                        role = mapRole(dto.currentUserCourseRole),
                    )
                }
                Log.d(TAG, "getMyCourses: success, size = ${courseDtos.size}")

                val userName = if (profileResponse.isSuccessful) {
                    val profile = profileResponse.body()
                    if (profile != null) "${profile.lastName} ${profile.firstName}" else "—"
                } else {
                    Log.d(TAG, "getMyProfile: error code = ${profileResponse.code()}")
                    "—"
                }

                val tasks = if (tasksResponse.isSuccessful) {
                    val dtos = tasksResponse.body().orEmpty()
                    Log.d(TAG, "getAllUserTaskAnswers: success, size = ${dtos.size}")
                    dtos.mapIndexed { index, dto ->
                        TaskUiItem(
                            id = dto.id,
                            postId = dto.postId,
                            courseId = dto.courseId,
                            title = dto.postName?.takeIf { it.isNotBlank() } ?: "Задание ${index + 1}",
                            status = when (dto.status) {
                                "COMPLETED" -> TaskStatus.SUBMITTED
                                "COMPETED_AFTER_DEADLINE" -> TaskStatus.SUBMITTED_LATE
                                "NOT_COMPLETED" -> TaskStatus.OVERDUE
                                else -> TaskStatus.NEW // "NEW"
                            },
                            score = dto.score?.toString(),
                            maxScore = dto.maxScore?.toString(),
                            deadline = null,
                            submittedAt = dto.submittedAt?.let { TaskDetailViewModel.formatIsoDate(it) },
                        )
                    }
                } else {
                    Log.d(TAG, "getAllUserTaskAnswers: error ${tasksResponse.code()}")
                    emptyList()
                }

                state = CoursesScreenState.Content(
                    courses = courses,
                    tasks = tasks,
                    userName = userName,
                )
            } catch (e: Exception) {
                state = CoursesScreenState.Error(e.message ?: "Неизвестная ошибка")
                Log.d(TAG, "loadData: exception", e)
            }
        }
    }

    companion object {
        private const val TAG = "CoursesScreen"

        private fun mapRole(apiRole: String?): String = when (apiRole) {
            "HEAD_TEACHER" -> "Главный преподаватель"
            "TEACHER" -> "Преподаватель"
            "STUDENT" -> "Студент"
            else -> ""
        }

    }
}
