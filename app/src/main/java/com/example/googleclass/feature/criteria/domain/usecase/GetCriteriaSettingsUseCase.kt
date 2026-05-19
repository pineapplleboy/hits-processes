package com.example.googleclass.feature.criteria.domain.usecase

import com.example.googleclass.feature.criteria.domain.model.CriteriaSettings
import com.example.googleclass.feature.post.data.model.supportsCriteria
import com.example.googleclass.feature.post.data.model.supportsCriteriaMultiplier
import com.example.googleclass.feature.post.domain.repository.PostRepository

class GetCriteriaSettingsUseCase(
    private val postRepository: PostRepository,
) {

    suspend operator fun invoke(
        courseId: String,
        postId: String,
    ): Result<CriteriaSettings> = postRepository.getPost(courseId, postId)
        .map { post ->
            CriteriaSettings(
                criteriaEnabled = post.taskMarkEvaluationType.supportsCriteria(),
                allowsMultiplier = post.taskMarkEvaluationType.supportsCriteriaMultiplier(),
            )
        }
}
