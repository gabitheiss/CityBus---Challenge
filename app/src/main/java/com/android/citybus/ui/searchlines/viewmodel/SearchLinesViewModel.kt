package com.android.citybus.ui.searchlines.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.citybus.domain.model.BusesLines
import com.android.citybus.repository.SearchLinesRepository
import java.net.HttpURLConnection.HTTP_OK
import kotlinx.coroutines.launch

class SearchLinesViewModel(
    private val repository: SearchLinesRepository,
) : ViewModel() {

    private val _linesLive = MutableLiveData<List<BusesLines>>()
    val linesLive: LiveData<List<BusesLines>> = _linesLive

    fun searchLines(searchTerm: String) {
        viewModelScope.launch {
            repository.getLines(searchTerm).let { response ->
                println("body result: ${response.body()}")
                if (response.code() == HTTP_OK) {
                    _linesLive.value = response.body()
                }
            }
        }
    }
}
