package com.android.citybus.ui.busesline.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPositionToLine
import com.android.citybus.repository.SearchLinesRepository
import kotlinx.coroutines.launch

class BusesLineViewModel(private val repository: SearchLinesRepository) : ViewModel() {

    private val _busesPositionToLineLive = MutableLiveData<BusesPositionToLine>()
    val busesPositionToLineLive: LiveData<BusesPositionToLine> = _busesPositionToLineLive
    private var line = ""

    companion object {
        private const val TOKEN = "9c1916e92687bbaf3be474e47214edd3822265ff008f8fc7867f051e787f3001"
        private var COOKIE: String = ""
    }

    fun getBusesPositionToLine(line: String) {
        this.line = line
        viewModelScope.launch {
            repository.getBusesPositionToLine(TOKEN, line, COOKIE).let { response ->
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
            with(repository.toAuthenticate(TOKEN)) {
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
        if (responseHeaderCookie != COOKIE) {
            println("cookies response: ${responseHeaderCookie}, $COOKIE")
            COOKIE = responseHeaderCookie ?: return
            getBusesPositionToLine(line)
        }
    }
}