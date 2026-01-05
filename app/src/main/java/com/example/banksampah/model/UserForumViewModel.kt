package com.example.banksampah.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.ForumPost
import com.example.banksampah.repository.ForumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserForumViewModel : ViewModel() {

    private val repository = ForumRepository()

    private val _postsState = MutableStateFlow<PostsState>(PostsState.Loading)
    val postsState: StateFlow<PostsState> = _postsState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    sealed class PostsState {
        object Loading : PostsState()
        data class Success(val posts: List<ForumPost>) : PostsState()
        data class Empty(val message: String) : PostsState()
        data class Error(val message: String) : PostsState()
    }

    sealed class DeleteState {
        object Idle : DeleteState()
        object Deleting : DeleteState()
        object Success : DeleteState()
        data class Error(val message: String) : DeleteState()
    }

    fun loadMyPosts() {
        viewModelScope.launch {
            _postsState.value = PostsState.Loading

            val result = repository.getMyPosts()

            if (result.isSuccess) {
                val posts = result.getOrThrow()
                _postsState.value = if (posts.isEmpty()) {
                    PostsState.Empty("Anda belum membuat postingan")
                } else {
                    PostsState.Success(posts)
                }
            } else {
                _postsState.value = PostsState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat postingan"
                )
            }
        }
    }

    fun loadPostsByUser(uid: String) {
        viewModelScope.launch {
            _postsState.value = PostsState.Loading

            val result = repository.getPostsByUser(uid)

            if (result.isSuccess) {
                val posts = result.getOrThrow()
                _postsState.value = if (posts.isEmpty()) {
                    PostsState.Empty("User belum membuat postingan")
                } else {
                    PostsState.Success(posts)
                }
            } else {
                _postsState.value = PostsState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat postingan"
                )
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Deleting

            val result = repository.deletePost(postId)

            if (result.isSuccess) {
                _deleteState.value = DeleteState.Success
                // Reload posts after delete
                loadMyPosts()
            } else {
                _deleteState.value = DeleteState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal menghapus postingan"
                )
            }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}