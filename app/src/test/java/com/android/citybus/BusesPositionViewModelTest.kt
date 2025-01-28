package com.android.citybus

import com.android.citybus.repository.BusesPositionRepository
import com.android.citybus.ui.busesposition.viewmodel.BusesPositionViewModel
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BusesPositionViewModelTest {

    @Mock
    private lateinit var repository: BusesPositionRepository

    private lateinit var viewModel: BusesPositionViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = BusesPositionViewModel(repository)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }

}