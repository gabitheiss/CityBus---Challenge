package com.android.citybus.di

import com.android.citybus.service.BusesService
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {

    single {
        provideService()
    }
}

fun provideService(): BusesService {
    return Retrofit.Builder()
        .baseUrl("https://api.olhovivo.sptrans.com.br/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(BusesService::class.java)
}