package com.android.citybus.ui.searchlines.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesLines
import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.session.SessionManager
import kotlinx.coroutines.launch

class SearchLinesViewModel(
    private val repository: SearchLinesRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _linesLive = MutableLiveData<List<BusesLines>>()
    val linesLive: LiveData<List<BusesLines>> = _linesLive
    private var searchTerm = ""

    fun searchLines(searchTerm: String) {
        this.searchTerm = searchTerm
        viewModelScope.launch {
            repository.getLines(searchTerm, sessionManager.cookie).let { response ->
                println("body result: ${response.body()}")
                when (response.code()) {
                    401 -> toAuthenticate()
                    200 -> _linesLive.value = response.body()
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
            searchLines(searchTerm)
        }
    }
}