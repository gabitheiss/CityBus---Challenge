package com.android.citybus

import android.app.Application
import com.android.citybus.di.repositoriesmodule.busesPositionRepositoryModule
import com.android.citybus.di.repositoriesmodule.searchLinesRepositoryModule
import com.android.citybus.di.servicesmodule.busesServiceModule
import com.android.citybus.di.viewmodelsmodule.busesLineViewModelModule
import com.android.citybus.di.viewmodelsmodule.busesPositionViewModelModule
import com.android.citybus.di.viewmodelsmodule.searchLinesViewModelModule
import org.koin.core.context.startKoin

class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                busesServiceModule,
                busesPositionRepositoryModule,
                searchLinesRepositoryModule,
                busesLineViewModelModule,
                busesPositionViewModelModule,
                searchLinesViewModelModule
            )
        }
    }
}