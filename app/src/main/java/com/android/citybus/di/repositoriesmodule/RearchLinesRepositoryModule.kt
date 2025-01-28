package com.android.citybus.di.repositoriesmodule

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.repository.SearchLinesRepositoryImpl
import com.android.citybus.service.BusesService
import org.koin.dsl.module

val searchLinesRepositoryModule = module {
    single {
        provideSearchLinesRepository(service = get())
    }
}

fun provideSearchLinesRepository(service: BusesService): SearchLinesRepository {
    return SearchLinesRepositoryImpl(service = service)
}