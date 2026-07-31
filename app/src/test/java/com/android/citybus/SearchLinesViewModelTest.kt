package com.android.citybus

import com.android.citybus.repository.SearchLinesRepository
import com.android.citybus.session.SessionManager
import com.android.citybus.session.SessionManagerImpl
import com.android.citybus.ui.searchlines.viewmodel.SearchLinesViewModel
import io.mockk.mockk
import junit.framework.TestCase.assertNotNull
import org.junit.Before
import org.junit.Test

class SearchLinesViewModelTest {

    private val repository: SearchLinesRepository = mockk(relaxed = true)
    private val sessionManager: SessionManager = SessionManagerImpl()

    private lateinit var viewModel: SearchLinesViewModel

    @Before
    fun setUp() {
        viewModel = SearchLinesViewModel(repository, sessionManager)
    }

    @Test
    fun `test ViewModel initialization`() {
        assertNotNull(viewModel)
    }
}
