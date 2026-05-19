package com.example.googleclass.feature.criteria

import com.example.googleclass.feature.criteria.data.api.CriteriaApi
import com.example.googleclass.feature.criteria.data.repository.CriteriaRepositoryImpl
import com.example.googleclass.feature.criteria.domain.repository.CriteriaRepository
import com.example.googleclass.feature.criteria.domain.usecase.CreateMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.DeleteMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetCriteriaSettingsUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.domain.usecase.GetTaskAnswerCriteriaScoresUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutCriteriaScoreUseCase
import com.example.googleclass.feature.criteria.domain.usecase.PutSelfAssessmentCriteriaScoreUseCase
import com.example.googleclass.feature.criteria.domain.usecase.UpdateMarkCriteriaUseCase
import com.example.googleclass.feature.criteria.presentation.CriteriaEvaluationViewModel
import com.example.googleclass.feature.criteria.presentation.CriteriaScoresViewModel
import com.example.googleclass.feature.criteria.presentation.CriteriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val criteriaModule = module {
    single<CriteriaRepository> { CriteriaRepositoryImpl(get<CriteriaApi>()) }

    factory { GetMarkCriteriaUseCase(get()) }
    factory { GetCriteriaSettingsUseCase(get()) }
    factory { CreateMarkCriteriaUseCase(get()) }
    factory { UpdateMarkCriteriaUseCase(get()) }
    factory { DeleteMarkCriteriaUseCase(get()) }
    factory { GetTaskAnswerCriteriaScoresUseCase(get()) }
    factory { PutCriteriaScoreUseCase(get()) }
    factory { PutSelfAssessmentCriteriaScoreUseCase(get()) }

    viewModel { (courseId: String, postId: String) ->
        CriteriaViewModel(
            courseId = courseId,
            postId = postId,
            getMarkCriteriaUseCase = get(),
            getCriteriaSettingsUseCase = get(),
            createMarkCriteriaUseCase = get(),
            updateMarkCriteriaUseCase = get(),
            deleteMarkCriteriaUseCase = get(),
        )
    }

    viewModel { (taskAnswerId: String, isSelfAssessment: Boolean) ->
        CriteriaScoresViewModel(
            taskAnswerId = taskAnswerId,
            isSelfAssessment = isSelfAssessment,
            getTaskAnswerCriteriaScoresUseCase = get(),
            putCriteriaScoreUseCase = get(),
            putSelfAssessmentCriteriaScoreUseCase = get(),
        )
    }

    viewModel { (courseId: String, postId: String, taskAnswerId: String) ->
        CriteriaEvaluationViewModel(
            courseId = courseId,
            postId = postId,
            taskAnswerId = taskAnswerId,
            postRepository = get(),
            taskAnswerRepository = get(),
            commentRepository = get(),
            getMarkCriteriaUseCase = get(),
            getTaskAnswerCriteriaScoresUseCase = get(),
            putCriteriaScoreUseCase = get(),
        )
    }
}
