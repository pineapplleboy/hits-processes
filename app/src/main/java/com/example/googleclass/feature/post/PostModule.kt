package com.example.googleclass.feature.post

import com.example.googleclass.feature.post.data.repository.PostRepositoryImpl
import com.example.googleclass.feature.post.domain.repository.PostRepository
import com.example.googleclass.feature.post.presentation.PostEditorMode
import com.example.googleclass.feature.post.presentation.PostEditorScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val postModule = module {

    single<PostRepository> { PostRepositoryImpl(get()) }

    viewModel { (mode: PostEditorMode) ->
        PostEditorScreenViewModel(
            mode = mode,
            postRepository = get(),
            fileRepository = get(),
            courseDetailRepository = get(),
            contentResolver = androidContext().contentResolver,
        )
    }
}
