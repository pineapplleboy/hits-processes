package com.example.googleclass.feature.post.data.model

fun TaskMarkEvaluationType?.supportsCriteria(): Boolean = when (this) {
    TaskMarkEvaluationType.SUM,
    TaskMarkEvaluationType.MEAN_VALUE,
    TaskMarkEvaluationType.COEFFICIENTS_SUM,
    TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE,
    TaskMarkEvaluationType.SELF_ASSESSMENT,
    TaskMarkEvaluationType.PASS_FAIL -> true
    else -> false
}

fun TaskMarkEvaluationType?.supportsCriteriaMultiplier(): Boolean = when (this) {
    TaskMarkEvaluationType.COEFFICIENTS_SUM,
    TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE -> true
    else -> false
}

fun TaskMarkEvaluationType?.usesBinaryTaskScore(): Boolean = when (this) {
    TaskMarkEvaluationType.TEACHER_DECISION_PASS_FAIL,
    TaskMarkEvaluationType.PASS_FAIL -> true
    else -> false
}
