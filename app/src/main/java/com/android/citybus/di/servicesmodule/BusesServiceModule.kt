package com.android.citybus.di.servicesmodule

import com.android.citybus.BuildConfig
import com.android.citybus.service.BusesService
import com.android.citybus.service.TokenInterceptor
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val busesServiceModule = module {

    single {
        provideService()
    }
}

fun provideService(): BusesService {
    val client = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(BuildConfig.SPTRANS_TOKEN))
        .build()

    return Retrofit.Builder()
        .baseUrl("https://api.olhovivo.sptrans.com.br/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(BusesService::class.java)
}