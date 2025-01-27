package com.android.citybus.di

import com.android.citybus.repository.BusesPositionRepository
import com.android.citybus.ui.busesposition.viewmodel.BusesPositionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel {
        provideBusesPositionViewModel(repository = get())
    }
}

fun provideBusesPositionViewModel(repository: BusesPositionRepository): BusesPositionViewModel {
    return BusesPositionViewModel(repository)

}