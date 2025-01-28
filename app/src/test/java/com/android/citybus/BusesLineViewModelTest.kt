package com.android.citybus

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.ui.busesline.viewmodel.BusesLineViewModel
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class BusesLineViewModelTest {

    @Mock
    lateinit var repository: SearchLinesRepository

    private lateinit var viewModel: BusesLineViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = BusesLineViewModel(repository)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }
}