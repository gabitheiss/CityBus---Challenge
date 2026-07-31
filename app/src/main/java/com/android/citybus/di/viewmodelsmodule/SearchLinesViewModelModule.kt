package com.android.citybus.di.viewmodelsmodule

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.session.SessionManager
import com.android.citybus.ui.searchlines.viewmodel.SearchLinesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val searchLinesViewModelModule = module {
    viewModel {
        provideSearchLinesViewModel(repository = get(), sessionManager = get())
    }
}

fun provideSearchLinesViewModel(
    repository: SearchLinesRepository,
    sessionManager: SessionManager,
): SearchLinesViewModel {
    return SearchLinesViewModel(repository, sessionManager)
}