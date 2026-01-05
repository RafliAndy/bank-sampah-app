package com.example.banksampah.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.User
import com.example.banksampah.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = ProfileRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val user: User, val isAdmin: Boolean) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Updating : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading

            try {
                val userResult = repository.getCurrentUser()
                val isAdminResult = repository.isUserAdmin()

                if (userResult.isSuccess && isAdminResult.isSuccess) {
                    _profileState.value = ProfileState.Success(
                        user = userResult.getOrThrow(),
                        isAdmin = isAdminResult.getOrThrow()
                    )
                } else {
                    _profileState.value = ProfileState.Error(
                        userResult.exceptionOrNull()?.message
                            ?: "Gagal memuat profil"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(
                    e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun updateDisplayName(newDisplayName: String) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Updating

            val result = repository.updateDisplayName(newDisplayName)

            if (result.isSuccess) {
                _updateState.value = UpdateState.Success
                loadProfile() // Reload profile
            } else {
                _updateState.value = UpdateState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal update nama"
                )
            }
        }
    }

    fun updateProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Updating

            try {
                // 1. Upload foto ke Storage
                val uploadResult = repository.uploadProfilePhoto(imageUri)

                if (uploadResult.isFailure) {
                    _updateState.value = UpdateState.Error(
                        uploadResult.exceptionOrNull()?.message
                            ?: "Gagal upload foto"
                    )
                    return@launch
                }

                val photoUrl = uploadResult.getOrThrow()

                // 2. Update URL di database
                val updateResult = repository.updateProfilePhotoUrl(photoUrl)

                if (updateResult.isSuccess) {
                    _updateState.value = UpdateState.Success
                    loadProfile() // Reload profile
                } else {
                    _updateState.value = UpdateState.Error(
                        updateResult.exceptionOrNull()?.message
                            ?: "Gagal update foto"
                    )
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(
                    e.message ?: "Terjadi kesalahan"
                )
            }
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Updating

            val result = repository.deleteProfilePhoto()

            if (result.isSuccess) {
                _updateState.value = UpdateState.Success
                loadProfile() // Reload profile
            } else {
                _updateState.value = UpdateState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal hapus foto"
                )
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}