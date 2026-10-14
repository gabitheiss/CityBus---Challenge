package com.android.citybus.di.sessionmodule

import com.android.citybus.session.SessionManager
import com.android.citybus.session.SessionManagerImpl
import org.koin.dsl.module

val sessionModule = module {
    single<SessionManager> { SessionManagerImpl() }
}
