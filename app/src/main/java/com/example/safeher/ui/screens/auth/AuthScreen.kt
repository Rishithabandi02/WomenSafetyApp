package com.example.safeher.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safeher.ui.components.GradientButton
import com.example.safeher.ui.components.PremiumTextField
import com.example.safeher.utils.NetworkUtils
import com.example.safeher.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.safeher.R

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onVerified: () -> Unit
) {
    var email by rememberSaveable  { mutableStateOf("") }
    var password by rememberSaveable  { mutableStateOf("") }
    var emailError by rememberSaveable  { mutableStateOf("") }
    var passwordError by rememberSaveable  { mutableStateOf("") }
    var isLoginMode by rememberSaveable  { mutableStateOf(true) }
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    val isLoading = authState is AuthViewModel.AuthState.Loading

    var showNoInternetDialog by rememberSaveable  {
        mutableStateOf(false)
    }

    LaunchedEffect(authState) {
        if (authState is AuthViewModel.AuthState.Verified) {
            onVerified()
        }
    }

    fun validate(): Boolean {
        var valid = true
        if (email.isBlank()) {
            emailError = "Email cannot be empty"
            valid = false
        } else if (email.length > 254) {
            emailError = "Email address is too long"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email"
            valid = false
        } else {
            emailError = ""
        }

        if (password.isBlank()) {
            passwordError = "Password cannot be empty"
            valid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            valid = false
        } else if (password.length > 128) {
            passwordError = "Password is too long"
            valid = false
        }else {
            passwordError = ""
        }

        return valid
    }
    if (showNoInternetDialog) {

        AlertDialog(
            onDismissRequest = {
                showNoInternetDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },

            title = {
                Text(
                    text = "No Internet Connection",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "HerBeacon couldn't connect to the internet. Please check your connection and try again."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showNoInternetDialog = false
                    }
                ) {

                    Text("OK")
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(R.drawable.safeher_logo),
            contentDescription = "HerBeacon Logo",
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "HerBeacon",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary

        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Safety Companion",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground

        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (isLoginMode) "Welcome back" else "Create account",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground

        )

        Spacer(modifier = Modifier.height(8.dp))


        PremiumTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
                authViewModel.clearAuthState()

            },
            label = "Email",
            isError = emailError.isNotEmpty(),
            errorText = emailError
        )

        Spacer(modifier = Modifier.height(4.dp))

        PremiumTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
                authViewModel.clearAuthState()

            },
            label = "Password",
            isPassword = true,
            isError = passwordError.isNotEmpty(),
            errorText = passwordError
        )
        if (isLoginMode) {
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (authState is AuthViewModel.AuthState.Error) {
            Text(
                text = (authState as AuthViewModel.AuthState.Error).msg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }


        GradientButton(
            text = if (isLoginMode) "Login" else "Create Account",
            isLoading = isLoading,
            onClick = {
                if (validate()) {
                    if (!NetworkUtils.isInternetAvailable(context)) {

                        showNoInternetDialog = true
                        return@GradientButton
                    }
                    if (isLoginMode)
                        authViewModel.login(email.trim(), password.trim())
                    else
                        authViewModel.signUp(email.trim(), password.trim())
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(onClick = {
            isLoginMode = !isLoginMode
            authViewModel.clearAuthState()
        }) {
            Text(
                text = if (isLoginMode) "Don't have an account? Create one"
                else "Already have an account? Login"
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
