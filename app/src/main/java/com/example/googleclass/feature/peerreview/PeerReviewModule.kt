package com.example.googleclass.feature.peerreview

import com.example.googleclass.feature.peerreview.data.api.PeerReviewApi
import com.example.googleclass.feature.peerreview.data.repository.PeerReviewRepositoryImpl
import com.example.googleclass.feature.peerreview.domain.repository.PeerReviewRepository
import com.example.googleclass.feature.peerreview.domain.usecase.GetAllAppraisersUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetAppraisersTopUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetAvailableWorksUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetMyAppraisersUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetPeerEvaluationDetailUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.GetTasksToAppraiseUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.OverrideAppraiserUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SelectWorkToAppraiseUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SubmitAppraiserCriteriaScoresUseCase
import com.example.googleclass.feature.peerreview.domain.usecase.SubmitAppraiserScoreUseCase
import com.example.googleclass.feature.peerreview.presentation.evaluate.PeerEvaluationViewModel
import com.example.googleclass.feature.peerreview.presentation.list.PeerReviewListViewModel
import com.example.googleclass.feature.peerreview.presentation.teacher.AppraisalsViewModel
import com.example.googleclass.feature.peerreview.presentation.top.AppraisersTopViewModel
import com.example.googleclass.feature.post.data.model.TaskAnswerAppraisingType
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val peerReviewModule = module {
    single<PeerReviewRepository> { PeerReviewRepositoryImpl(get<PeerReviewApi>()) }

    factory { GetTasksToAppraiseUseCase(get()) }
    factory { GetAvailableWorksUseCase(get()) }
    factory { SelectWorkToAppraiseUseCase(get()) }
    factory { GetPeerEvaluationDetailUseCase(get()) }
    factory { SubmitAppraiserScoreUseCase(get()) }
    factory { SubmitAppraiserCriteriaScoresUseCase(get()) }
    factory { GetMyAppraisersUseCase(get()) }
    factory { GetAllAppraisersUseCase(get()) }
    factory { OverrideAppraiserUseCase(get()) }
    factory { GetAppraisersTopUseCase(get()) }

    viewModel { (courseId: String, postId: String, appraisingType: TaskAnswerAppraisingType?) ->
        PeerReviewListViewModel(
            courseId = courseId,
            postId = postId,
            appraisingType = appraisingType,
            getTasksToAppraiseUseCase = get(),
            getAvailableWorksUseCase = get(),
            selectWorkToAppraiseUseCase = get(),
        )
    }

    viewModel { (courseId: String, postId: String, evaluationId: String) ->
        PeerEvaluationViewModel(
            courseId = courseId,
            postId = postId,
            evaluationId = evaluationId,
            postRepository = get(),
            getPeerEvaluationDetailUseCase = get(),
            getMarkCriteriaUseCase = get(),
            submitAppraiserScoreUseCase = get(),
            submitAppraiserCriteriaScoresUseCase = get(),
        )
    }

    viewModel { (courseId: String, postId: String, taskAnswerId: String) ->
        AppraisalsViewModel(
            courseId = courseId,
            postId = postId,
            taskAnswerId = taskAnswerId,
            getAllAppraisersUseCase = get(),
            overrideAppraiserUseCase = get(),
            getMarkCriteriaUseCase = get(),
        )
    }

    viewModel { (courseId: String) ->
        AppraisersTopViewModel(
            courseId = courseId,
            getAppraisersTopUseCase = get(),
        )
    }
}
