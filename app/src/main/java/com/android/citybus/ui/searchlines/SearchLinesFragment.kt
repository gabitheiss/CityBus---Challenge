package com.android.citybus.ui.searchlines

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.citybus.R
import com.android.citybus.databinding.FragmentSearchBinding
import com.android.citybus.ext.gone
import com.android.citybus.ext.replaceFragmentWithAnimation
import com.android.citybus.ext.visible
import com.android.citybus.ui.busesline.BusesLineFragment
import com.android.citybus.ui.searchlines.adapter.BusesLinesAdapterListener
import com.android.citybus.ui.searchlines.adapter.SearchLinesAdapter
import com.android.citybus.ui.searchlines.viewmodel.SearchLinesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchLinesFragment: Fragment(), BusesLinesAdapterListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchLinesAdapter

    private val viewModel by viewModel<SearchLinesViewModel>()

    companion object {
        @JvmStatic
        fun newInstance(): SearchLinesFragment = SearchLinesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        with(binding) {
            inputLayoutSearch.setEndIconOnClickListener { _ ->
                adapter.clear()
                notFoundView.gone()
                inputSearch.text?.clear()
            }
            inputSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                   //do nothing
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    //do nothing
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().length >= 4) {
                        viewModel.searchLines(s.toString())
                        notFoundView.gone()
                        busesLineListView.gone()
                        loadingView.visible()
                    }
                }
            })

            with(viewModel) {
                linesLive.observeForever {
                    adapter = SearchLinesAdapter(it, this@SearchLinesFragment, requireContext())
                    if (it.isNullOrEmpty()) {
                        notFoundView.visible()
                        loadingView.gone()
                    } else {
                        busesLineListView.adapter = adapter
                        busesLineListView.visible()
                        notFoundView.gone()
                        loadingView.gone()
                    }
                }
            }
        }
    }

    override fun onLineClick(line: String) {
        context?.let {
            (activity as AppCompatActivity).replaceFragmentWithAnimation(BusesLineFragment.newInstance(line), R.id.containerSearch, true)
        }
    }
}