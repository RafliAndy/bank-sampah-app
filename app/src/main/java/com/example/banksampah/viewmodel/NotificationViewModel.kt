package com.example.banksampah.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.Notification
import com.example.banksampah.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val repository = NotificationRepository()

    private val _notificationsState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val notificationsState: StateFlow<NotificationsState> = _notificationsState

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    sealed class NotificationsState {
        object Loading : NotificationsState()
        data class Success(val notifications: List<Notification>) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }

    init {
        loadNotifications()
        startListeningToNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _notificationsState.value = NotificationsState.Loading

            val result = repository.getUserNotifications()

            if (result.isSuccess) {
                val notifications = result.getOrThrow()
                _notificationsState.value = NotificationsState.Success(notifications)

                // Update unread count
                val unread = notifications.count { !it.isRead }
                _unreadCount.value = unread
            } else {
                _notificationsState.value = NotificationsState.Error(
                    result.exceptionOrNull()?.message ?: "Gagal memuat notifikasi"
                )
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadCount()
            if (result.isSuccess) {
                _unreadCount.value = result.getOrThrow()
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
            loadNotifications() // Refresh
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            repository.markAllAsRead()
            loadNotifications() // Refresh
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
            loadNotifications() // Refresh
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            repository.deleteAllNotifications()
            loadNotifications() // Refresh
        }
    }

    private fun startListeningToNotifications() {
        repository.listenToNotifications(
            onUpdate = { notifications ->
                _notificationsState.value = NotificationsState.Success(notifications)
                _unreadCount.value = notifications.count { !it.isRead }
            },
            onError = { error ->
                _notificationsState.value = NotificationsState.Error(error)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup jika diperlukan
    }
}