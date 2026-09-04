package com.android.citybus

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.ui.busesline.viewmodel.BusesLineViewModel
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test

class BusesLineViewModelTest {

    private val repository: SearchLinesRepository = mockk(relaxed = true)

    private lateinit var viewModel: BusesLineViewModel

    @Before
    fun setUp() {
        viewModel = BusesLineViewModel(repository)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }
}
