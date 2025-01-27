package com.android.citybus.ui.busesposition.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesPosition
import com.android.citybus.repository.BusesPositionRepository
import com.google.android.gms.maps.model.LatLng
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

    @SuppressLint("MissingPermission")
        fun getLocationUser(context: Context): LatLng {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            var lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            return if (lastKnownLocation != null) {
                LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            } else {
                LatLng(-23.560372327785654, -46.65070732268567)
            }
        } catch (e: SecurityException) {
            Log.e("Localização", "Erro ao acessar localização: ${e.message}")
            return LatLng(-23.560372327785654, -46.65070732268567)
        }
    }
}