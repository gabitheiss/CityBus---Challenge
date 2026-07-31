package com.android.citybus.ui.busesline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPositionToLine
import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.session.SessionManager
import kotlinx.coroutines.launch

class BusesLineViewModel(
    private val repository: SearchLinesRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _busesPositionToLineLive = MutableLiveData<BusesPositionToLine>()
    val busesPositionToLineLive: LiveData<BusesPositionToLine> = _busesPositionToLineLive
    private var line = ""

    fun getBusesPositionToLine(line: String) {
        this.line = line
        viewModelScope.launch {
            repository.getBusesPositionToLine(line, sessionManager.cookie).let { response ->
                println("body result: ${response.body()}")
                when (response.code()) {
                    401 -> toAuthenticate()
                    200 -> _busesPositionToLineLive.value = response.body()
                }
            }
        }
    }

    private fun toAuthenticate() {
        viewModelScope.launch {
            with(repository.toAuthenticate()) {
                println("body result authenticate: ${this.body()}")
                when (this.body()) {
                    true -> handleSuccessfulAuthentication(this.headers()["set-cookie"])
                    else ->  {
                        //do nothing
                    }
                }
            }
        }
    }

    private fun handleSuccessfulAuthentication(responseHeaderCookie: String?) {
        if (responseHeaderCookie != sessionManager.cookie) {
            println("cookies response: ${responseHeaderCookie}, ${sessionManager.cookie}")
            sessionManager.cookie = responseHeaderCookie ?: return
            getBusesPositionToLine(line)
        }
    }
}