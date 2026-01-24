package com.example.banksampah.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.example.banksampah.repository.UserManagementRepository
import com.example.banksampah.repository.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserManagementViewModel : ViewModel() {

    private val repository = UserManagementRepository()

    private val _usersState = MutableStateFlow<UsersState>(UsersState.Loading)
    val usersState: StateFlow<UsersState> = _usersState

    private val _statsState = MutableStateFlow<StatsState>(StatsState.Loading)
    val statsState: StateFlow<StatsState> = _statsState

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    sealed class UsersState {
        object Loading : UsersState()
        data class Success(val users: List<User>) : UsersState()
        data class Error(val message: String) : UsersState()
    }

    sealed class StatsState {
        object Loading : StatsState()
        data class Success(val stats: UserStats) : StatsState()
        data class Error(val message: String) : StatsState()
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        object Success : OperationState()
        data class Error(val message: String) : OperationState()
    }

    init {
        loadUsers()
        loadStats()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = UsersState.Loading

            val result = repository.getAllUsers()

            _usersState.value = if (result.isSuccess) {
                UsersState.Success(result.getOrThrow())
            } else {
                UsersState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat daftar user"
                )
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = StatsState.Loading

            val result = repository.getUserStats()

            _statsState.value = if (result.isSuccess) {
                StatsState.Success(result.getOrThrow())
            } else {
                StatsState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat statistik"
                )
            }
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.updateUserRole(userId, newRole)

            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadUsers() // Refresh list
                loadStats() // Refresh stats
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal mengubah role"
                )
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.deleteUser(userId)

            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadUsers() // Refresh list
                loadStats() // Refresh stats
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal menghapus user"
                )
            }
        }
    }

    fun suspendUser(userId: String, isSuspended: Boolean) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val result = repository.suspendUser(userId, isSuspended)

            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadUsers() // Refresh list
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal suspend user"
                )
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}