package com.example.banksampah.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    // State khusus untuk reset password
    private val _resetPasswordState = MutableLiveData<ResetPasswordState>()
    val resetPasswordState: LiveData<ResetPasswordState> = _resetPasswordState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.LoggedIn
        } else {
            _authState.value = AuthState.LoggedOut
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.LoggedIn
                } else {
                    _authState.value = AuthState.Error("Gagal login: ${task.exception?.message}")
                }
            }
    }

    fun signup(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password tidak boleh kosong")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password minimal 6 karakter")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.LoggedIn
                } else {
                    _authState.value = AuthState.Error("Gagal mendaftar: ${task.exception?.message}")
                }
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.LoggedOut
    }

    // ===== FORGOT PASSWORD FUNCTIONS =====

    /**
     * Kirim email reset password ke email user
     * Firebase akan otomatis mengirim email dengan link reset password
     */
    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = ResetPasswordState.Error("Email tidak boleh kosong")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _resetPasswordState.value = ResetPasswordState.Error("Format email tidak valid")
            return
        }

        _resetPasswordState.value = ResetPasswordState.Loading

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _resetPasswordState.value = ResetPasswordState.Success(
                    "Email reset password telah dikirim ke $email. Silakan cek inbox atau folder spam Anda."
                )
            }
            .addOnFailureListener { exception ->
                val errorMessage = when (exception.message) {
                    "There is no user record corresponding to this identifier. The user may have been deleted." ->
                        "Email tidak terdaftar di sistem"
                    "The email address is badly formatted." ->
                        "Format email tidak valid"
                    else -> "Gagal mengirim email: ${exception.message}"
                }
                _resetPasswordState.value = ResetPasswordState.Error(errorMessage)
            }
    }

    /**
     * Reset state untuk reset password
     * Digunakan saat user meninggalkan screen atau setelah menampilkan pesan
     */
    fun resetPasswordResetState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    // ===== AUTH STATE =====
    sealed class AuthState {
        object Loading : AuthState()
        object LoggedIn : AuthState()
        object LoggedOut : AuthState()
        data class Error(val message: String) : AuthState()
    }

    // ===== RESET PASSWORD STATE =====
    sealed class ResetPasswordState {
        object Idle : ResetPasswordState()
        object Loading : ResetPasswordState()
        data class Success(val message: String) : ResetPasswordState()
        data class Error(val message: String) : ResetPasswordState()
    }
}