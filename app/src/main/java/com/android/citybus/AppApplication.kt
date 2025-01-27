package com.android.citybus

import android.app.Application
import com.android.citybus.di.apiModule
import com.android.citybus.di.repositoriesModule
import com.android.citybus.di.repositorySearchModule
import com.android.citybus.di.searchLinesViewModelsModule
import com.android.citybus.di.viewModelsModule
import org.koin.core.context.startKoin

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                apiModule,
                repositoriesModule,
                repositorySearchModule,
                viewModelsModule,
                searchLinesViewModelsModule
            )
        }
    }
}