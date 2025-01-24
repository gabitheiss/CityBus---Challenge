package com.android.citybus.ui.busesposition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.citybus.databinding.FragmentBusesPositionBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusesPositionFragment : Fragment() {

    private var _binding: FragmentBusesPositionBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<BusesPositionViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() = BusesPositionFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentBusesPositionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.busesPositionLive.observeForever {
            println(it.lines)
        }

        binding.buttonTest.setOnClickListener {
            viewModel.getBusesPosition()
        }
    }
}