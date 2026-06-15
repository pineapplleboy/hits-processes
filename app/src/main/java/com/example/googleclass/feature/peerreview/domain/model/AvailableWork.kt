package com.example.googleclass.feature.peerreview.domain.model

/**
 * Работа другого студента, доступная (или нет) для самостоятельного выбора
 * на взаимное оценивание.
 */
data class AvailableWork(
    val taskAnswerId: String,
    val studentId: String?,
    val studentName: String?,
    val submittedAt: String?,
    val canAppraise: Boolean,
    val unavailableReason: UnavailableReason?,
    val files: List<PeerReviewFile> = emptyList(),
)

enum class UnavailableReason {
    TASK_DEADLINE_HAS_NOT_PASSED,
    APPRAISER_DEADLINE_HAS_PASSED,
    ANSWER_IS_NOT_SUBMITTED,
    OWN_ANSWER,
    ALREADY_SELECTED,
    APPRAISING_LIMIT_REACHED,
    RECIPROCAL_APPRAISING,
    UNKNOWN;

    companion object {
        fun fromRaw(raw: String?): UnavailableReason? =
            raw?.let { value -> entries.firstOrNull { it.name == value } ?: UNKNOWN }
    }
}
