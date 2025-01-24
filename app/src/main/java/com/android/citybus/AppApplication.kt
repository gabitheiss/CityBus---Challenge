package com.android.citybus

import android.app.Application
import com.android.citybus.di.apiModule
import com.android.citybus.di.repositoriesModule
import com.android.citybus.di.viewModelsModule
import org.koin.core.context.startKoin

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(apiModule, repositoriesModule, viewModelsModule)
        }
    }
}