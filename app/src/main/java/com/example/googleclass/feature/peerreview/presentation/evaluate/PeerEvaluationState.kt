package com.example.googleclass.feature.peerreview.presentation.evaluate

import com.example.googleclass.feature.peerreview.domain.model.PeerReviewFile
import com.example.googleclass.feature.post.data.model.TaskMarkEvaluationType

sealed interface PeerEvaluationUiState {

    data object Loading : PeerEvaluationUiState

    data class Content(
        val studentName: String,
        val files: List<PeerReviewFile>,
        val usesCriteria: Boolean,
        val evaluationType: TaskMarkEvaluationType?,
        val taskMaxScore: Float,
        val criteria: List<PeerCriterionField>,
        val singleScoreInput: String,
        val calculatedScore: Float?,
        val isSaving: Boolean = false,
    ) : PeerEvaluationUiState

    data class Error(val message: String) : PeerEvaluationUiState
}

data class PeerCriterionField(
    val markCriteriaId: String,
    val name: String,
    val description: String?,
    val minScore: Float,
    val maxScore: Float,
    val multiplier: Float?,
    val input: String,
)

sealed interface PeerEvaluationUiEvent {
    data object NavigateBack : PeerEvaluationUiEvent
    data object Save : PeerEvaluationUiEvent
    data class CriterionScoreChanged(val markCriteriaId: String, val value: String) : PeerEvaluationUiEvent
    data class SingleScoreChanged(val value: String) : PeerEvaluationUiEvent
}

sealed interface PeerEvaluationUiEffect {
    data object NavigateBack : PeerEvaluationUiEffect
    data class ShowMessage(val message: String) : PeerEvaluationUiEffect
}
