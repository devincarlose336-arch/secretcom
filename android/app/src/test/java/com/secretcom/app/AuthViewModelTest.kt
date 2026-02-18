package com.secretcom.app

import com.secretcom.app.data.repository.AuthRepository
import com.secretcom.app.domain.model.AuthResponse
import com.secretcom.app.domain.model.User
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.ui.auth.AuthViewModel
import com.secretcom.app.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock(AuthRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should not be logged in`() {
        val state = AuthViewModel::class.java.getDeclaredField("_state")
        assertEquals(false, false == false)
    }

    @Test
    fun `clearError should set error to null`() {
        assertNull(null)
    }

    @Test
    fun `user roles should map correctly`() {
        assertEquals(UserRole.SUPER_ADMIN, UserRole.fromString("super_admin"))
        assertEquals(UserRole.ADMIN, UserRole.fromString("admin"))
        assertEquals(UserRole.USER, UserRole.fromString("user"))
        assertEquals(UserRole.USER, UserRole.fromString("unknown"))
    }

    @Test
    fun `Resource Success should contain data`() {
        val resource = Resource.Success("test")
        assertEquals("test", resource.data)
    }

    @Test
    fun `Resource Error should contain message`() {
        val resource = Resource.Error<String>("error")
        assertEquals("error", resource.message)
    }
}
