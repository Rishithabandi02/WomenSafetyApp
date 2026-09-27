package com.example.safeher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {

    var showAboutDialog by remember{
        mutableStateOf(false)
    }

    var showContactDialog by remember{
        mutableStateOf(false)
    }

    var showPrivacyDialog by remember {
        mutableStateOf(false)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item{
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Mode",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Switch(
                            checked = darkMode,

                            onCheckedChange = {
                                onThemeChange(it)
                            }
                        )
                    }
                }
            }

            item{
                SettingsItem(
                    title = "About Us",
                    icon = Icons.Default.Info,
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }
            item{
                SettingsItem(
                    title = "Contact Us",
                    icon = Icons.Default.Email,
                    onClick = {
                        showContactDialog = true
                    }
                )
            }

            item{
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {
                        showPrivacyDialog = true
                    }
                )
            }

            item{
                HorizontalDivider()
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                SettingsItem(
                    title = "Logout",
                    icon = Icons.Default.Logout,
                    onClick = {
                        showLogoutDialog = true
                    }
                )
            }
        }

        if(showAboutDialog){
            AlertDialog(
                onDismissRequest = {
                    showAboutDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAboutDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                title = {
                    Text("About HerBeacon")
                },

                text = {
                    Text(
                        "HerBeacon is a women safety application that helps users stay connected with trusted contacts through SOS alerts, live location tracking and emergency assistance."
                    )
                }

            )
        }

        if(showContactDialog){
            AlertDialog(
                onDismissRequest = {
                    showContactDialog = false
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showContactDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                title = {
                    Text("Contact Us")
                },
                text = {
                    Text(
                        "For support or feedback:\n\nherbeacon.app@gmail.com"
                    )
                }
            )
        }
        if (showPrivacyDialog) {

            AlertDialog(
                onDismissRequest = {
                    showPrivacyDialog = false
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showPrivacyDialog = false
                        }
                    ) {
                        Text("OK")
                    }
                },

                title = {
                    Text("Privacy Policy")
                },

                text = {

                    Text(
                        "HerBeacon only uses your location during active SOS tracking sessions. Your emergency contacts and tracking information are securely stored and used solely for safety purposes."
                    )
                }
            )
        }
        if (showLogoutDialog) {

            AlertDialog(
                onDismissRequest = {
                    showLogoutDialog = false
                },

                title = {
                    Text("Logout")
                },

                text = {
                    Text(
                        "Are you sure you want to logout?"
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {

                            showLogoutDialog = false

                            onLogout()
                        }
                    ) {

                        Text("Logout")
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}