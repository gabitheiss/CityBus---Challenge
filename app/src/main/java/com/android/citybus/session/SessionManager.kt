package com.android.citybus.session

interface SessionManager {
    var cookie: String
}

class SessionManagerImpl : SessionManager {

    @Volatile
    override var cookie: String = ""
}
