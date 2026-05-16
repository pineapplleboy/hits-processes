package com.example.googleclass.feature.courses.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CourseCreateDto(
    val name: String,
    val description: String,
    val courseMarkEvaluationType: CourseMarkEvaluationType = CourseMarkEvaluationType.SUM,
    val passThreshold: Float? = null,
)

@Serializable
enum class CourseMarkEvaluationType {
    SUM,
    MEAN_VALUE,
    COEFFICIENTS_SUM,
    COEFFICIENTS_MEAN_VALUE,
    PASS_FAIL,
}

