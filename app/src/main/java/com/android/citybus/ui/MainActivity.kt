package com.android.citybus.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.citybus.R
import com.android.citybus.databinding.ActivityMainBinding
import com.android.citybus.ui.busesposition.BusesPositionFragment
import com.android.citybussp.ext.replaceFragmentWithAnimation

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        replaceFragmentWithAnimation(BusesPositionFragment.newInstance(), R.id.containerView, true)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }
}