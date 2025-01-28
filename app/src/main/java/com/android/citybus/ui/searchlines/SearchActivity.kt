package com.android.citybus.ui.searchlines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.android.citybus.R
import com.android.citybus.databinding.ActivitySearchBinding
import com.android.citybus.ext.replaceFragmentWithAnimation

class SearchActivity : AppCompatActivity() {

    private var _binding: ActivitySearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragmentWithAnimation(SearchLinesFragment.newInstance(), R.id.containerSearch, true)
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        super.onBackPressed()
    }
}