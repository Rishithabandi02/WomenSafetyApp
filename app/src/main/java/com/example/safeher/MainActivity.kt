package com.example.safeher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.safeher.ui.theme.SafeHerTheme
import com.example.safeher.navigation.NavGraph
import com.example.safeher.utils.ThemePreferences
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val smsGranted =
                permissions[Manifest.permission.SEND_SMS] ?: false

            if (!smsGranted) {
                Toast.makeText(
                    this,
                    "SMS permission is required to send SOS alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val themePrefs = remember { ThemePreferences(this) }
            var darkMode by remember { mutableStateOf(themePrefs.isDarkMode()) }


            SafeHerTheme(darkTheme = darkMode) {
                NavGraph(
                    darkMode = darkMode,
                    onThemeChange = { enabled ->
                        darkMode = enabled
                        themePrefs.setDarkMode(enabled)
                    }

                )
            }
        }
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

