package com.example.googleclass.feature.courses.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.googleclass.feature.courses.data.remote.CoursesApi
import kotlinx.coroutines.launch

class CoursesScreenViewModel(
    private val coursesApi: CoursesApi,
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                val response = coursesApi.getMyCourses(isArchived = false)
                if (response.isSuccessful) {
                    Log.d(TAG, "getMyCourses: success, body size = ${response.body()?.size ?: 0}")
                } else {
                    Log.d(TAG, "getMyCourses: error code = ${response.code()}, message = ${response.message()}")
                }
            } catch (e: Exception) {
                Log.d(TAG, "getMyCourses: exception", e)
            }
        }
    }

    companion object {
        private const val TAG = "CoursesScreen"
    }
}
