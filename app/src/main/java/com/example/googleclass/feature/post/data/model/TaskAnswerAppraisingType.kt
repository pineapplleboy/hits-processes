package com.example.googleclass.feature.post.data.model

import kotlinx.serialization.Serializable

/**
 * Способ распределения работ между студентами-оценщиками при взаимном оценивании.
 *
 * CHAIN — работы назначаются по цепочке автоматически,
 * ANY — студент сам выбирает, какие работы оценивать.
 */
@Serializable
enum class TaskAnswerAppraisingType {
    CHAIN,
    ANY,
}
