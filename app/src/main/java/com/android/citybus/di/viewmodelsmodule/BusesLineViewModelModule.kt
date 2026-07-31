package com.android.citybus.di.viewmodelsmodule

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.session.SessionManager
import com.android.citybus.ui.busesline.viewmodel.BusesLineViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val busesLineViewModelModule = module {
    viewModel {
        provideBusesLineViewModel(repository = get(), sessionManager = get())
    }
}

fun provideBusesLineViewModel(
    repository: SearchLinesRepository,
    sessionManager: SessionManager,
): BusesLineViewModel {
    return BusesLineViewModel(repository, sessionManager)
}