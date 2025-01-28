package com.android.citybus.ui.busesposition

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.citybus.R
import com.android.citybus.databinding.ActivityMapsBinding
import com.android.citybus.ext.gone
import com.android.citybus.ext.visible
import com.android.citybus.ui.busesposition.viewmodel.BusesPositionViewModel
import com.android.citybus.ui.searchlines.SearchActivity
import com.android.citybus.util.getLocationUser
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusesPositionMapActivity : AppCompatActivity() {

    private var _binding: ActivityMapsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<BusesPositionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapCustomView.initializeMap(this, getLocationUser(this))
    }

    override fun onStart() {
        super.onStart()

        with(binding) {
            viewModel.apply {
                getBusesPosition()
                busesPositionLive.observeForever { busesPosition ->
                    mapCustomView.apply {
                        clearItemsCluster()
                        addMarkersAllBusesInMap(busesPosition)
                        cluster()
                        setTextTitle(this@BusesPositionMapActivity.getString(R.string.title_map))
                    }
                    loadingView.gone()
                    mapViewGroup.visible()
                }
            }

            updateButtonView.setOnClickListener {
                viewModel.getBusesPosition()
                loadingView.visible()
                mapViewGroup.gone()

            }

            searchButtonView.setOnClickListener {
                Intent(this@BusesPositionMapActivity, SearchActivity::class.java).apply {
                    startActivity(this)
                }
            }
        }
    }
}