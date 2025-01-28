package com.android.citybus

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.ui.searchlines.viewmodel.SearchLinesViewModel
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SearchLinesViewModelTest {

    @Mock
    private lateinit var repository: SearchLinesRepository

    private lateinit var viewModel: SearchLinesViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = SearchLinesViewModel(repository)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }
}