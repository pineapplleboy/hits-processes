package com.example.googleclass.feature.course.presentation.marks

sealed interface MarksScreenState {
    data object Loading : MarksScreenState
    data class Error(val message: String) : MarksScreenState
    data class Content(val students: List<StudentMarkItem>) : MarksScreenState
}
