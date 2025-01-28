package com.android.citybus.di.repositoriesmodule

import com.android.citybus.repository.BusesPositionRepository
import com.android.citybus.repository.BusesPositionRepositoryImpl
import com.android.citybus.service.BusesService
import org.koin.dsl.module

val busesPositionRepositoryModule = module {
    single {
        provideBusesPositionRepository(service = get())
    }
}

fun provideBusesPositionRepository(service: BusesService): BusesPositionRepository {
    return BusesPositionRepositoryImpl(service = service)
}