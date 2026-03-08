package com.example.googleclass.feature.courses.presentation

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.common.network.UserApi
import com.example.googleclass.feature.courses.data.remote.CourseCreateDto
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CoursesScreenViewModel(
    private val coursesApi: CoursesApi,
    private val userApi: UserApi,
) : ViewModel() {

    var state by mutableStateOf<CoursesScreenState>(CoursesScreenState.Loading)
        private set

    var dialogState by mutableStateOf<CreateCourseDialogState?>(null)
        private set

    init {
        loadData()
    }

    // region Dialog

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

    private fun loadData() {
        state = CoursesScreenState.Loading
        viewModelScope.launch {
            try {
                val profileDeferred = async { userApi.getMyProfile() }
                val coursesDeferred = async { coursesApi.getMyCourses(isArchived = false) }

                val profileResponse = profileDeferred.await()
                val coursesResponse = coursesDeferred.await()

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
                        role = "Студент",
                        code = dto.id.take(6).uppercase(),
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

                state = CoursesScreenState.Content(
                    courses = courses,
                    tasks = emptyList(),
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
    }
}
