package com.example.banksampah.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth


class AuthViewModel : ViewModel() {
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState


    init {
        checkAuthStatus()
    }


    fun checkAuthStatus() {
        if(auth.currentUser != null) {
            _authState.value = AuthState.LoggedIn
        }else{
            _authState.value = AuthState.LoggedOut
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.LoggedIn
                }
                else {
                    _authState.value = AuthState.Error("Gagal login: ${task.exception?.message}")
                }
            }
    }

    fun signup(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.LoggedIn
                }
                else {
                    _authState.value = AuthState.Error("Gagal login: ${task.exception?.message}")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.LoggedOut
    }

    sealed class AuthState {
        object Loading : AuthState()
        object LoggedIn : AuthState()
        object LoggedOut : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
