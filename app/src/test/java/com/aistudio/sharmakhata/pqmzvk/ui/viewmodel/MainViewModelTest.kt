package com.aistudio.sharmakhata.pqmzvk.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainViewModelTest {

    @Test
    fun `viewModel can be instantiated`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNotNull("MainViewModel should be created without crashing", viewModel)
    }

    @Test
    fun `initial operationState is Idle`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue(
            "Initial operation state should be Idle",
            viewModel.operationState.value is OperationState.Idle
        )
    }

    @Test
    fun `initial lastCreatedBillId is null`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNull("No bill created yet", viewModel.lastCreatedBillId.value)
    }

    @Test
    fun `initial authToken is null`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNull("Not authenticated initially", viewModel.authToken.value)
    }

    @Test
    fun `initial isOffline is false`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertEquals("Should start as online", false, viewModel.isOffline.value)
    }

    @Test
    fun `initial items list is empty`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue("Items should be empty initially", viewModel.items.value.isEmpty())
    }

    @Test
    fun `initial expenses list is empty`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue("Expenses should be empty initially", viewModel.expenses.value.isEmpty())
    }

    @Test
    fun `initial todayTotal is zero`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertEquals(0.0, viewModel.todayTotal.value, 0.001)
    }

    @Test
    fun `initial pendingCount is zero`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertEquals(0, viewModel.pendingCount.value)
    }

    @Test
    fun `initial logoutEvent is false`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertEquals("Should not be logged out initially", false, viewModel.logoutEvent.value)
    }

    @Test
    fun `initial droppedOps list is empty`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue("Dropped operations should be empty initially", viewModel.droppedOps.value.isEmpty())
    }

    @Test
    fun `initial dbState is Loading`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue(
            "Database state should start as Loading",
            viewModel.dbState.value is UiState.Loading
        )
    }

    @Test
    fun `initial reportState is Loading`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertTrue(
            "Report state should start as Loading",
            viewModel.reportState.value is UiState.Loading
        )
    }

    @Test
    fun `resetOperationState sets state back to Idle`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        viewModel.resetOperationState()
        assertTrue(
            "After reset, operation state should be Idle",
            viewModel.operationState.value is OperationState.Idle
        )
    }

    @Test
    fun `consumeLogoutEvent resets flag`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        viewModel.consumeLogoutEvent()
        assertEquals(false, viewModel.logoutEvent.value)
    }

    @Test
    fun `consumeAuthToken returns null when no token set`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNull(viewModel.consumeAuthToken())
    }

    @Test
    fun `consumeRegisteredStoreId returns null when no store registered`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MainViewModel(app)
        assertNull(viewModel.consumeRegisteredStoreId())
    }
}
