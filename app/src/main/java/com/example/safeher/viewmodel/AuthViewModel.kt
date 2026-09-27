package com.example.safeher.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.safeher.data.firebase.RealtimeDatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private var isNewUser = false
    private val dbHelper = RealtimeDatabaseHelper()


    sealed class AuthState{
        object Idle : AuthState()
        object Loading : AuthState()
        object Verified : AuthState()
        object ProfileSaved : AuthState()
        data class Error(val msg: String) : AuthState()
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState : StateFlow<AuthState> = _authState

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                isNewUser = true
                _authState.value = AuthState.Verified
            }
            .addOnFailureListener { e ->

                if (e is FirebaseAuthException) {
                    Log.d("SAFEHER_AUTH", "ErrorCode: ${e.errorCode}")
                }
                Log.d("SAFEHER_AUTH", "Message: ${e.message}")

                val message = when (e) {

                    is FirebaseAuthUserCollisionException ->
                        "An account with this email already exists."

                    is FirebaseAuthWeakPasswordException ->
                        "Password must be at least 6 characters long."

                    is FirebaseAuthInvalidCredentialsException -> {
                        when (e.errorCode) {
                            "ERROR_INVALID_EMAIL" ->
                                "Please enter a valid email address."

                            else ->
                                "Invalid email address."
                        }
                    }

                    is FirebaseNetworkException ->
                        "No internet connection. Please check your internet and try again."

                    else ->
                        "Signup failed. Please try again."
                }

                _authState.value = AuthState.Error(message)
            }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                isNewUser = false
                _authState.value = AuthState.Verified
            }

            .addOnFailureListener { e ->

                Log.d("SAFEHER_AUTH", "Exception: ${e.javaClass.simpleName}")

                if (e is FirebaseAuthException) {
                    Log.d("SAFEHER_AUTH", "ErrorCode: ${e.errorCode}")
                }

                Log.d("SAFEHER_AUTH", "Message: ${e.message}")

                val message = when (e) {

                    is FirebaseAuthInvalidCredentialsException -> {
                        when (e.errorCode) {

                            "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                            // Covers wrong password + non-existent email
                            "ERROR_INVALID_CREDENTIAL" -> "Invalid Email or Password."
                            else -> "Invalid Email or Password."
                        }
                    }

                    is FirebaseNetworkException -> "No internet connection. Please check your internet and try again."

                    is FirebaseTooManyRequestsException -> "Too many failed login attempts. Please try again later."

                    else -> "Login failed. Please try again."
                }

                _authState.value = AuthState.Error(message)
            }

    }
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    fun saveName(name: String, onSuccess: () -> Unit) {
        if (name.isBlank()) {
            _authState.value = AuthState.Error("Please enter your name")
            return
        }
        _authState.value = AuthState.Loading
        val email = auth.currentUser?.email ?: ""
        dbHelper.saveUserProfile(
            name = name,
            email = email,
            onSuccess = {
                _authState.value = AuthState.ProfileSaved
                onSuccess()
            },
            onFailure = { e ->
                _authState.value = AuthState.Error(e)
            }
        )

    }

    fun logOut(){
        auth.signOut()
        _authState.value = AuthState.Idle
    }
    fun isNewUser(): Boolean = isNewUser

}