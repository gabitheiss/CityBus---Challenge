package com.android.citybus.ui.busesposition.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.repository.BusesPositionRepository
import java.net.HttpURLConnection.HTTP_OK
import kotlinx.coroutines.launch

class BusesPositionViewModel(
    private val repository: BusesPositionRepository,
) : ViewModel() {

    private val _busesPositionLive = MutableLiveData<BusesPosition>()
    val busesPositionLive: LiveData<BusesPosition> = _busesPositionLive

    fun getBusesPosition() {
        viewModelScope.launch {
            repository.getBusesPosition().let { response ->
                println("body result: ${response.body()}")
                if (response.code() == HTTP_OK) {
                    _busesPositionLive.value = response.body() ?: return@let
                }
            }
        }
    }
}
