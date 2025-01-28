package com.android.citybus.ui.busesposition.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.repository.BusesPositionRepository
import kotlinx.coroutines.launch

class BusesPositionViewModel(private val repository: BusesPositionRepository) : ViewModel() {

    private val _busesPositionLive = MutableLiveData<BusesPosition>()
    val busesPositionLive: LiveData<BusesPosition> = _busesPositionLive

    companion object {
        private const val TOKEN = "9c1916e92687bbaf3be474e47214edd3822265ff008f8fc7867f051e787f3001"
        private var COOKIE: String = ""
    }

    fun getBusesPosition() {
        viewModelScope.launch {
            repository.getBusesPosition(TOKEN, COOKIE).let { response ->
                println("body result: ${response.body()}")
                when (response.code()) {
                    401 -> toAuthenticate()
                    200 -> _busesPositionLive.value = response.body() ?: return@let
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
            getBusesPosition()
        }
    }
}