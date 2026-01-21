package com.example.banksampah.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.ActivityAlbum
import com.example.banksampah.data.AlbumPhoto
import com.example.banksampah.repository.AlbumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlbumViewModel : ViewModel() {
    private val repository = AlbumRepository()

    private val _albumsState = MutableStateFlow<AlbumsState>(AlbumsState.Loading)
    val albumsState: StateFlow<AlbumsState> = _albumsState

    private val _photosState = MutableStateFlow<PhotosState>(PhotosState.Loading)
    val photosState: StateFlow<PhotosState> = _photosState

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState

    sealed class AlbumsState {
        object Loading : AlbumsState()
        data class Success(val albums: List<ActivityAlbum>) : AlbumsState()
        data class Error(val message: String) : AlbumsState()
    }

    sealed class PhotosState {
        object Loading : PhotosState()
        data class Success(val photos: List<AlbumPhoto>) : PhotosState()
        data class Error(val message: String) : PhotosState()
    }

    sealed class OperationState {
        object Idle : OperationState()
        object Loading : OperationState()
        object Success : OperationState()
        data class Error(val message: String) : OperationState()
    }

    init {
        loadAlbums()
    }

    // ========== ALBUM OPERATIONS ==========

    fun loadAlbums() {
        viewModelScope.launch {
            _albumsState.value = AlbumsState.Loading
            val result = repository.getAllAlbums()
            _albumsState.value = if (result.isSuccess) {
                AlbumsState.Success(result.getOrThrow())
            } else {
                AlbumsState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat album")
            }
        }
    }

    fun getAlbumById(albumId: String, callback: (Result<ActivityAlbum>) -> Unit) {
        viewModelScope.launch {
            val result = repository.getAlbumById(albumId)
            callback(result)
        }
    }

    fun addAlbum(
        title: String,
        description: String,
        date: String,
        location: String,
        coverImageUri: Uri?
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            try {
                val coverUrl = if (coverImageUri != null) {
                    val uploadResult = repository.uploadImage(coverImageUri)
                    if (uploadResult.isFailure) {
                        _operationState.value = OperationState.Error("Gagal upload gambar")
                        return@launch
                    }
                    uploadResult.getOrThrow()
                } else {
                    ""
                }

                val album = ActivityAlbum(
                    title = title,
                    description = description,
                    date = date,
                    location = location,
                    coverImageUrl = coverUrl
                )

                val result = repository.addAlbum(album)
                if (result.isSuccess) {
                    _operationState.value = OperationState.Success
                    loadAlbums()
                } else {
                    _operationState.value = OperationState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal menambahkan album"
                    )
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun updateAlbum(
        album: ActivityAlbum,
        newCoverImageUri: Uri?
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            try {
                val finalAlbum = if (newCoverImageUri != null) {
                    val uploadResult = repository.uploadImage(newCoverImageUri)
                    if (uploadResult.isFailure) {
                        _operationState.value = OperationState.Error("Gagal upload gambar")
                        return@launch
                    }
                    album.copy(coverImageUrl = uploadResult.getOrThrow())
                } else {
                    album
                }

                val result = repository.updateAlbum(finalAlbum)
                if (result.isSuccess) {
                    _operationState.value = OperationState.Success
                    loadAlbums()
                } else {
                    _operationState.value = OperationState.Error(
                        result.exceptionOrNull()?.message ?: "Gagal update album"
                    )
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun deleteAlbum(albumId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = repository.deleteAlbum(albumId)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadAlbums()
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal hapus album"
                )
            }
        }
    }

    // ========== PHOTO OPERATIONS ==========

    fun loadAlbumPhotos(albumId: String) {
        viewModelScope.launch {
            _photosState.value = PhotosState.Loading
            val result = repository.getAlbumPhotos(albumId)
            _photosState.value = if (result.isSuccess) {
                PhotosState.Success(result.getOrThrow())
            } else {
                PhotosState.Error(result.exceptionOrNull()?.message ?: "Gagal memuat foto")
            }
        }
    }

    fun addPhotoToAlbum(
        albumId: String,
        caption: String,
        imageUri: Uri,
        order: Int = 0
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading

            val uploadResult = repository.uploadImage(imageUri)
            if (uploadResult.isFailure) {
                _operationState.value = OperationState.Error("Gagal upload foto")
                return@launch
            }

            val photo = AlbumPhoto(
                albumId = albumId,
                imageUrl = uploadResult.getOrThrow(),
                caption = caption,
                order = order
            )

            val result = repository.addPhotoToAlbum(photo)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadAlbumPhotos(albumId)
                loadAlbums() // Refresh to update photo count
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal menambahkan foto"
                )
            }
        }
    }

    fun deletePhoto(photoId: String, albumId: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = repository.deletePhoto(photoId, albumId)
            if (result.isSuccess) {
                _operationState.value = OperationState.Success
                loadAlbumPhotos(albumId)
                loadAlbums() // Refresh to update photo count
            } else {
                _operationState.value = OperationState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal hapus foto"
                )
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}