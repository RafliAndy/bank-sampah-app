package com.example.banksampah.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.EdukasiItem
import com.example.banksampah.data.GalleryItem
import com.example.banksampah.repository.EdukasiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EdukasiViewModel : ViewModel() {
    private val repository = EdukasiRepository()

    private val _edukasiState = MutableStateFlow<EdukasiState>(EdukasiState.Loading)
    val edukasiState: StateFlow<EdukasiState> = _edukasiState

    private val _galleryState = MutableStateFlow<GalleryState>(GalleryState.Loading)
    val galleryState: StateFlow<GalleryState> = _galleryState

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    sealed class EdukasiState {
        object Loading : EdukasiState()
        data class Success(val edukasiList: List<EdukasiItem>) : EdukasiState()
        data class Error(val message: String) : EdukasiState()
    }

    sealed class GalleryState {
        object Loading : GalleryState()
        data class Success(val galleryList: List<GalleryItem>) : GalleryState()
        data class Error(val message: String) : GalleryState()
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        object Success : OperationState()
        data class Error(val message: String) : OperationState()
    }

    init {
        loadEdukasi()
    }

    fun loadEdukasi() {
        viewModelScope.launch {
            _edukasiState.value = EdukasiState.Loading
            val result = repository.getAllEdukasi()
            _edukasiState.value = if (result.isSuccess) {
                EdukasiState.Success(result.getOrThrow())
            } else {
                EdukasiState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat edukasi")
            }
        }
    }

    fun loadGallery() {
        viewModelScope.launch {
            _galleryState.value = GalleryState.Loading
            val result = repository.getAllGallery()
            _galleryState.value = if (result.isSuccess) {
                GalleryState.Success(result.getOrThrow())
            } else {
                GalleryState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat galeri")
            }
        }
    }

    fun getEdukasiById(id: String, callback: (Result<EdukasiItem>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getEdukasiById(id)
            callback(result)
        }
    }

    fun addEdukasi(edukasi: EdukasiItem, imageUri: Uri?) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            try {
                val finalEdukasi = if (imageUri != null) {
                    val uploadResult = repository.uploadImage(imageUri)
                    if (uploadResult.isFailure) {
                        _operationState.value = OperationState.Error("Gagal upload gambar")
                        return@launch
                    }
                    edukasi.copy(imageUrl = uploadResult.getOrThrow())
                } else {
                    edukasi
                }

                val result = repository.addEdukasi(finalEdukasi)
                if (result.isSuccess) {
                    _operationState.value = OperationState.Success
                    loadEdukasi()
                } else {
                    _operationState.value = OperationState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal menambahkan edukasi"
                    )
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updateEdukasi(edukasi: EdukasiItem, imageUri: Uri?) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            try {
                val finalEdukasi = if (imageUri != null) {
                    val uploadResult = repository.uploadImage(imageUri)
                    if (uploadResult.isFailure) {
                        _operationState.value = OperationState.Error("Gagal upload gambar")
                        return@launch
                    }
                    edukasi.copy(imageUrl = uploadResult.getOrThrow())
                } else {
                    edukasi
                }

                val result = repository.updateEdukasi(finalEdukasi)
                if (result.isSuccess) {
                    _operationState.value = OperationState.Success
                    loadEdukasi()
                } else {
                    _operationState.value = OperationState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal update edukasi"
                    )
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun deleteEdukasi(id: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = repository.deleteEdukasi(id)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadEdukasi()
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal hapus edukasi"
                )
            }
        }
    }

    // Gallery operations
    fun addGalleryItem(description: String, imageUri: Uri) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val uploadResult = repository.uploadImage(imageUri)
            if (uploadResult.isFailure) {
                _operationState.value = OperationState.Error("Gagal upload gambar")
                return@launch
            }

            val gallery = GalleryItem(
                imageUrl = uploadResult.getOrThrow(),
                description = description
            )

            val result = repository.addGalleryItem(gallery)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadGallery()
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal menambahkan galeri"
                )
            }
        }
    }

    fun deleteGalleryItem(id: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = repository.deleteGalleryItem(id)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadGallery()
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal hapus galeri"
                )
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}