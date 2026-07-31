package com.android.citybus.ui.busesposition.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.repository.BusesPositionRepository
import com.android.citybus.session.SessionManager
import kotlinx.coroutines.launch

class BusesPositionViewModel(
    private val repository: BusesPositionRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _busesPositionLive = MutableLiveData<BusesPosition>()
    val busesPositionLive: LiveData<BusesPosition> = _busesPositionLive

    fun getBusesPosition() {
        viewModelScope.launch {
            repository.getBusesPosition(sessionManager.cookie).let { response ->
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
           with(repository.toAuthenticate()) {
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
            sessionManager.cookie = responseHeaderCookie ?: return
            getBusesPosition()
        }
    }
}