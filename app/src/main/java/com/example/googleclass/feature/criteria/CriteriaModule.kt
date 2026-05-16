package com.example.googleclass.feature.criteria

import com.example.googleclass.feature.criteria.presentation.CriteriaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val criteriaModule = module {
    viewModel {
        CriteriaViewModel()
    }
}
