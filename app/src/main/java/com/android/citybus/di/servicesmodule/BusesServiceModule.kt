package com.android.citybus.di.servicesmodule

import com.android.citybus.BuildConfig
import com.android.citybus.service.AuthInterceptor
import com.android.citybus.service.BusesService
import com.android.citybus.service.TokenInterceptor
import com.android.citybus.session.SessionManager
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val busesServiceModule = module {

    single {
        provideService(get())
    }
}

fun provideService(session: SessionManager): BusesService {
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(session))
        .addInterceptor(TokenInterceptor(BuildConfig.SPTRANS_TOKEN))
        .build()

    return Retrofit.Builder()
        .baseUrl("https://api.olhovivo.sptrans.com.br/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(BusesService::class.java)
}
