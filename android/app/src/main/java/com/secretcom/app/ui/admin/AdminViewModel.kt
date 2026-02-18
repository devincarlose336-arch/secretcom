package com.secretcom.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secretcom.app.data.local.dao.RecordingDao
import com.secretcom.app.data.local.entity.RecordingEntity
import com.secretcom.app.data.repository.AdminRepository
import com.secretcom.app.data.repository.AuthRepository
import com.secretcom.app.domain.model.DashboardData
import com.secretcom.app.domain.model.User
import com.secretcom.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val isLoading: Boolean = false,
    val dashboard: DashboardData? = null,
    val users: List<User> = emptyList(),
    val admins: List<User> = emptyList(),
    val recordings: List<RecordingEntity> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository,
    private val recordingDao: RecordingDao
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state

    init {
        loadDashboard()
        loadRecordings()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getDashboard()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, dashboard = result.data) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getUsers()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, users = result.data) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadAdmins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.getAdmins()) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, admins = result.data) }
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
            _state.update { it.copy(isLoading = true) }
            when (val result = authRepository.createAdmin(username, password, name)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(isLoading = false, successMessage = "Admin created successfully")
                    }
                    loadAdmins()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleUserStatus(userId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.toggleUserStatus(userId)) {
                is Resource.Success -> loadUsers()
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.deleteUser(userId)) {
                is Resource.Success -> {
                    _state.update { it.copy(successMessage = "User deleted") }
                    loadUsers()
                }
                is Resource.Error -> {
                    _state.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun generateMeetingIds() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminRepository.generateMeetingIds()) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(isLoading = false, successMessage = result.data)
                    }
                    loadDashboard()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadRecordings() {
        viewModelScope.launch {
            recordingDao.getAllRecordings().collect { recordings ->
                _state.update { it.copy(recordings = recordings) }
            }
        }
    }

    fun deleteRecording(recording: RecordingEntity) {
        viewModelScope.launch {
            val file = java.io.File(recording.filePath)
            if (file.exists()) file.delete()
            recordingDao.deleteRecording(recording)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun clearSuccess() {
        _state.update { it.copy(successMessage = null) }
    }
}
