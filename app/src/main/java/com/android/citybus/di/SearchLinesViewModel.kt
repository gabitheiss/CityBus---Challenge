package com.android.citybus.di

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.ui.searchlines.viewmodel.SearchLinesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchLinesViewModelsModule = module {
    viewModel {
        provideSearchLinesViewModel(repository = get())
    }
}

fun provideSearchLinesViewModel(repository: SearchLinesRepository): SearchLinesViewModel {
    return SearchLinesViewModel(repository)
}