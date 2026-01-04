package com.example.banksampah.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.banksampah.data.NewsItem
import com.example.banksampah.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState

    sealed class NewsState {
        object Loading : NewsState()
        data class Success(val news: List<NewsItem>) : NewsState()
        data class Error(val message: String, val errorType: NewsRepository.ErrorType) : NewsState()
    }

    fun loadNews() {
        viewModelScope.launch {
            _newsState.value = NewsState.Loading

            when (val result = repository.fetchLatestNews()) {
                is NewsRepository.NewsResult.Success -> {
                    _newsState.value = NewsState.Success(result.news)
                }
                is NewsRepository.NewsResult.Error -> {
                    _newsState.value = NewsState.Error(result.message, result.type)
                }
            }
        }
    }
}