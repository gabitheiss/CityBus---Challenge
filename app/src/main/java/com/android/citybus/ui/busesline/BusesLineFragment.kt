package com.android.citybus.ui.busesline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.citybus.R
import com.android.citybus.databinding.FragmentBusesLineBinding
import com.android.citybus.ext.gone
import com.android.citybus.ext.visible
import com.android.citybus.ui.busesline.viewmodel.BusesLineViewModel
import com.android.citybus.util.getLocationUser
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusesLineFragment: Fragment() {

    private var _binding: FragmentBusesLineBinding? = null
    private val binding get() = _binding!!
    private lateinit var lines: String

    private val viewModel by viewModel<BusesLineViewModel>()

    companion object {
        const val EXTRA_LINE_TO_BUSES = "EXTRA_LINE_TO_BUSES"

        @JvmStatic
        fun newInstance(busesLines: String): BusesLineFragment =
            BusesLineFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_LINE_TO_BUSES, busesLines)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBusesLineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun onStart() {
        super.onStart()
        with(binding) {
            mapLineCustomView.initializeMap((activity as AppCompatActivity), getLocationUser(requireContext()))
            viewModel.apply {
                getBusesPositionToLine(lines)
                busesPositionToLineLive.observeForever {
                    mapLineCustomView.apply {
                        if (it.buses.isEmpty()) {
                            gone()
                            notFoundView.visible()
                        } else {
                            clearItemsCluster()
                            addMarkersBusesLineInMap(it)
                            cluster()
                            setTextTitle(this@BusesLineFragment.getString(R.string.title_line_map))
                        }
                    }
                }
            }
        }

    }

    private fun initData() {
        arguments?.let { bundle ->
            bundle.getString(EXTRA_LINE_TO_BUSES)?.let {
                lines = it
            }
        }
    }
}