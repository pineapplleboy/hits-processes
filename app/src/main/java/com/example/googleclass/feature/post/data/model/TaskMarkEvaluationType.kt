package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TaskMarkEvaluationType {
    TEACHER_DECISION,
    TEACHER_DECISION_PASS_FAIL,
    SUM,
    MEAN_VALUE,
    COEFFICIENTS_SUM,
    COEFFICIENTS_MEAN_VALUE,
    SELF_ASSESSMENT,
    PASS_FAIL,
}
