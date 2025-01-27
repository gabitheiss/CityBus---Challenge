package com.android.citybus.di

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.repository.SearchLinesRepositoryImpl
import com.android.citybus.service.BusesService
import org.koin.dsl.module

val repositorySearchModule = module {
    single {
        provideSearchLinesRepository(service = get())
    }
}

fun provideSearchLinesRepository(service: BusesService): SearchLinesRepository {
    return SearchLinesRepositoryImpl(service = service)
}