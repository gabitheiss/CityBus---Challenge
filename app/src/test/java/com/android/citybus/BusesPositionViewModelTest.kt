package com.android.citybus

import com.android.citybus.repository.BusesPositionRepository
import com.android.citybus.ui.busesposition.viewmodel.BusesPositionViewModel
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test

class BusesPositionViewModelTest {

    private val repository: BusesPositionRepository = mockk(relaxed = true)

    private lateinit var viewModel: BusesPositionViewModel

    @Before
    fun setUp() {
        viewModel = BusesPositionViewModel(repository)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }
}
