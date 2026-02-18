package com.secretcom.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secretcom.app.data.repository.AuthRepository
import com.secretcom.app.domain.model.UserRole
import com.secretcom.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userRole: UserRole? = null,
    val userName: String = "",
    val meetingId: String? = null,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false,
    val createAdminSuccess: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            if (loggedIn) {
                val role = authRepository.getCurrentRole()
                val name = authRepository.getCurrentUserName() ?: ""
                val meetingId = authRepository.getCurrentMeetingId()
                _state.update {
                    it.copy(isLoggedIn = true, userRole = role, userName = name, meetingId = meetingId)
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(username, password)) {
                is Resource.Success -> {
                    val user = result.data.user
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            loginSuccess = true,
                            userRole = user.role,
                            userName = user.name,
                            meetingId = user.meetingId,
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun register(name: String, meetingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(name, meetingId)) {
                is Resource.Success -> {
                    val user = result.data.user
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            registerSuccess = true,
                            userRole = user.role,
                            userName = user.name,
                            meetingId = user.meetingId,
                        )
                    }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun createAdmin(username: String, password: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.createAdmin(username, password, name)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, createAdminSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = AuthState()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun resetSuccess() {
        _state.update {
            it.copy(loginSuccess = false, registerSuccess = false, createAdminSuccess = false)
        }
    }
}
