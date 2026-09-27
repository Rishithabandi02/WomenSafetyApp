package com.example.safeher.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.safeher.ui.screens.SplashScreen
//import com.example.safeher.HomeScreen
import com.example.safeher.ui.screens.auth.AuthScreen
import com.example.safeher.ui.screens.auth.NameScreen
import com.example.safeher.ui.screens.contacts.ContactsScreen
import com.example.safeher.ui.screens.home.HomeScreen
import com.example.safeher.ui.screens.maps.MapsScreen
import com.example.safeher.ui.settings.SettingsScreen
import com.example.safeher.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    darkMode : Boolean,
    onThemeChange : (Boolean) -> Unit
){
    val navController = rememberNavController()
    val authViewModel : AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {

            SplashScreen {
                if (authViewModel.isUserLoggedIn()) {
                    navController.navigate("home") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }

                } else {

                    navController.navigate("auth") {
                        popUpTo("splash") {
                            inclusive = true
                        }
                    }
                }
            }
        }
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onVerified = {
                    if(authViewModel.isNewUser()){
                        navController.navigate("name"){
                            popUpTo("auth"){
                                inclusive = true
                            }
                        }
                    }
                    else {
                        navController.navigate("home"){
                            popUpTo("auth"){ inclusive = true}
                        }
                    }
                }
            )
        }
        composable("name") {
            NameScreen(
                authViewModel = authViewModel,
                onProfileSaved = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                authViewModel,
                onLogout = {
                    navController.navigate("auth"){
                        popUpTo("home"){ inclusive = true}
                    }
                },
                onContactsClick = {
                    navController.navigate("contacts")
                },
                onMapClick = { userId, sessionId ->
                    navController.navigate("maps/$userId/$sessionId")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }

            )

        }

        composable("contacts") {
            ContactsScreen()
        }

        composable("maps/{userId}/{sessionId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            MapsScreen(userId = userId, sessionId = sessionId)


        }

        composable("settings") {
            SettingsScreen(
                darkMode = darkMode,
                onThemeChange = onThemeChange,
                onLogout = {
                    authViewModel.logOut()
                    navController.navigate("auth"){
                        popUpTo(0)

                    }
                }
            )
        }
    }
}