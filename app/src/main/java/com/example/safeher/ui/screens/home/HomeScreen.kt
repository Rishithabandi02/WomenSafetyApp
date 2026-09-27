package com.example.safeher.ui.screens.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.safeher.data.firebase.RealtimeDatabaseHelper
import com.example.safeher.services.LocationTrackingService
import com.example.safeher.services.SOSManager
import com.example.safeher.services.ShakeDetector
import com.example.safeher.ui.components.GradientButton
import com.example.safeher.ui.components.PremiumSOSButton
import com.example.safeher.ui.components.QuickActionCard
import com.example.safeher.utils.LocationHelper
import com.example.safeher.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import com.example.safeher.utils.NetworkUtils
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sms
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onContactsClick: () -> Unit,
    onMapClick: (String, String) -> Unit,
    onSettingsClick : () -> Unit
) {

    var locationText by rememberSaveable {
        mutableStateOf("Location not fetched yet")
    }

    val context = LocalContext.current

    val locationHelper = remember {
        LocationHelper(context)
    }

    var isTracking by rememberSaveable {
        mutableStateOf(false)
    }

    val dbHelper = remember {
        RealtimeDatabaseHelper()
    }

    var sessionId by rememberSaveable {
        mutableStateOf("")
    }

    var showCountdown by rememberSaveable {
        mutableStateOf(false)
    }

    var countdownValue by rememberSaveable {
        mutableStateOf(5)
    }

    var showSafeSent by rememberSaveable {
        mutableStateOf(false)
    }

    var showStopDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    var showLocationSheet by rememberSaveable {
        mutableStateOf(false)
    }

    var currentAddress by rememberSaveable {
        mutableStateOf("Fetching location...")
    }

    var currentLat by rememberSaveable {
        mutableStateOf(0.0)
    }

    var currentLng by rememberSaveable {
        mutableStateOf(0.0)
    }

    var showNoContactsDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showNoContactsSafeDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showNoSimDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showNoInternetDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showSmsPermissionDialog by rememberSaveable {
        mutableStateOf(false)
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
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
    val smsPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (!isGranted) {
                Toast.makeText(
                    context,
                    "SMS permission is required to send SOS alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    if (showSmsPermissionDialog) {

        AlertDialog(
            onDismissRequest = {
                showSmsPermissionDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },

            title = {
                Text(
                    text = "SMS Permission Required",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "HerBeacon needs SMS permission to send emergency alerts to your trusted contacts."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showSmsPermissionDialog = false

                        smsPermissionLauncher.launch(
                            Manifest.permission.SEND_SMS
                        )
                    }
                ) {
                    Text("Grant Permission")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showSmsPermissionDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(currentUser?.uid) {

        currentUser?.uid?.let { userId ->
            dbHelper.observeActiveSession(userId) { activeId ->
                sessionId = activeId ?: ""
                isTracking = !activeId.isNullOrBlank()

            }
        }
    }

    var username by remember {
        mutableStateOf("")
    }
    LaunchedEffect(currentUser?.uid) {

        if (currentUser != null) {

            dbHelper.getUserProfile(
                onResult = { name, _ ->
                    username = name
                },
                onFailure = {
                }
            )
        }
    }
    fun triggerSOS() {

        val userId = currentUser?.uid ?: ""
        val newSessionId = "session_${System.currentTimeMillis()}"
        sessionId = newSessionId

        val trackingUrl = "https://herbeacon-track.netlify.app?userId=$userId&sessionId=$newSessionId"
        dbHelper.saveTrackingSession(userId, newSessionId)

        val intent = Intent(
            context,
            LocationTrackingService::class.java
        ).apply {

            putExtra(LocationTrackingService.EXTRA_USER_ID, userId)
            putExtra(LocationTrackingService.EXTRA_SESSION_ID, newSessionId)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        val sosManager = SOSManager(context)

        dbHelper.getContacts(
            onResult = { contacts ->

                val phoneNumbers = contacts.map { it.phone }
                sosManager.sendSOSMessages(
                    phoneNumbers,
                    trackingUrl,
                    onSmsUnavailable = {
                        showNoSimDialog = true
                    }
                )
            },

            onFailure = { error ->
                Log.e("SafeHer", "Failed to fetch contacts: $error"
                )
            }
        )
        isTracking = true
    }

    val shakeDetector = remember {

        ShakeDetector(context) {
            if (!isTracking && !showCountdown) {
                showCountdown = true
            }
        }
    }

    DisposableEffect(Unit) {

        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }

    val locationSettingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            locationText = "Try fetching location again"
        }

    LaunchedEffect(showCountdown) {

        if (showCountdown) {
            countdownValue = 5

            for (i in 5 downTo 1) {
                countdownValue = i
                delay(1000L)
            }

            if (showCountdown) {
                showCountdown = false
                triggerSOS()
            }
        }
    }

    if (showCountdown) {

        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "⚠️ SOS Activating",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },

            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = countdownValue.toString(),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Sending SOS to your emergency contacts..."
                    )
                }
            },

            confirmButton = { },

            dismissButton = {
                Button(
                    onClick = {
                        showCountdown = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "CANCEL",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    if (showNoContactsDialog) {

        AlertDialog(
            onDismissRequest = {
                showNoContactsDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },

            title = {
                Text(
                    text = "No Emergency Contacts",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "HerBeacon couldn't find any emergency contacts.\n\nPlease add at least one trusted contact before using SOS so they can receive your emergency alerts."
                )
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showNoContactsDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showNoContactsDialog = false
                        onContactsClick()
                    }
                ) {
                    Text(
                        text = "Add Contacts",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    if (showNoContactsSafeDialog) {

        AlertDialog(
            onDismissRequest = {
                showNoContactsSafeDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },

            title = {
                Text(
                    text = "No Emergency Contacts",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "Before you can send an \"I'm Safe\" message, add at least one trusted emergency contact."
                )
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showNoContactsSafeDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showNoContactsSafeDialog = false
                        onContactsClick()
                    }
                ) {
                    Text(
                        text = "Add Contacts",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
    if (showNoSimDialog) {

        AlertDialog(
            onDismissRequest = {
                showNoSimDialog = false
            },

            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },

            title = {
                Text("SMS Unavailable")
            },

            text = {
                Text(
                    "HerBeacon couldn't send emergency SMS because no active SIM card was detected.\n\n Your emergency contacts were not notified."
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showNoSimDialog = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    fun fetchLocation() {

        if (locationHelper.isLocationEnabled()) {
            locationText = "Fetching location..."
            locationHelper.getLastLocation(
                onResult = { lat, lng ->

                    currentLat = lat
                    currentLng = lng
                    locationText = "Location fetched"
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(
                                lat,
                                lng,
                                1
                            )
                        val address = addresses?.firstOrNull()
                        currentAddress = listOfNotNull(
                            address?.subLocality,
                            address?.locality,
                            address?.adminArea
                        ).joinToString(", ")

                    } catch (e: Exception) {
                        currentAddress = "Unable to fetch address"
                    }

                },

                onFailure = {
                    locationText = "Couldn't fetch location, try again!"
                }
            )

        } else {
            locationText = "Please turn on Location in settings"
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            locationSettingsLauncher.launch(intent)
        }
    }


    val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                fetchLocation()
            } else {
                locationText = "Permission denied. Cannot fetch location."
            }
        }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = {
                        onSettingsClick()
                    }
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                HeaderSection(
                    userName = username,
                )
                Spacer(modifier = Modifier.height(5.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PremiumSOSButton(
                        isTracking = isTracking,

                        onClick = {
                            if (!NetworkUtils.isInternetAvailable(context)) {

                                showNoInternetDialog = true
                                return@PremiumSOSButton
                            }
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.SEND_SMS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                showSmsPermissionDialog = true
                                return@PremiumSOSButton
                            }

                            if (!isTracking) {

                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    locationPermissionLauncher.launch(
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                    return@PremiumSOSButton
                                }
                                if (!locationHelper.isLocationEnabled()) {
                                    Toast.makeText(
                                        context,
                                        "Please enable location services first",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    return@PremiumSOSButton
                                }
                                dbHelper.getContacts(

                                    onResult = { contacts ->

                                        if (contacts.isEmpty()) {
                                            showNoContactsDialog = true
                                            return@getContacts
                                        }

                                        showCountdown = true
                                    },

                                    onFailure = {
                                        Toast.makeText(
                                            context,
                                            "Unable to fetch emergency contacts.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )

                            } else {
                                showStopDialog = true
                            }
                        }
                    )

                }
                Text(
                    text = "Your live location is shared with your emergency contacts for up to 30 minutes during an active SOS.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                Spacer(modifier = Modifier.height(1.dp))


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        QuickActionCard(
                            title = "Where Am I",
                            icon = Icons.Default.LocationOn,
                            onClick = {
                                showLocationSheet = true
                                fetchLocation()
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Contacts",
                            icon = Icons.Default.Phone,
                            onClick = onContactsClick,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        QuickActionCard(
                            title = "I'm Safe",
                            icon = Icons.Default.Favorite,
                            onClick = {
                                if (!NetworkUtils.isInternetAvailable(context)) {

                                    showNoInternetDialog = true
                                    return@QuickActionCard
                                }

                                val sosManager = SOSManager(context)

                                dbHelper.getContacts(

                                    onResult = { contacts ->

                                        if (contacts.isEmpty()) {
                                            showNoContactsSafeDialog = true
                                            return@getContacts
                                        }

                                        val phoneNumbers = contacts.map {
                                            it.phone
                                        }

                                        sosManager.sendSafeMessage(
                                            contacts = phoneNumbers,
                                            onSuccess = {
                                                showSafeSent = true
                                                Toast.makeText(
                                                    context,
                                                    "Safe message sent ️",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onSmsUnavailable = {
                                                showNoSimDialog = true
                                                return@sendSafeMessage
                                            }
                                        )

                                    },

                                    onFailure = { error ->
                                        Log.e(
                                            "SafeHer",
                                            "Failed to fetch contacts: $error"
                                        )
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Live Map",
                            icon = Icons.Default.Info,
                            onClick = {
                                if (sessionId.isNotBlank()) {

                                    onMapClick(
                                        currentUser?.uid ?: "",
                                        sessionId
                                    )

                                } else {

                                    Toast.makeText(
                                        context,
                                        "Start SOS tracking first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }


            }
            if (showStopDialog) {

                AlertDialog(
                    onDismissRequest = {
                        showStopDialog = false
                    },

                    title = {
                        Text(
                            text = "Stop SOS?"
                        )
                    },

                    text = {
                        Text(
                            text = "Are you sure you want to stop emergency tracking?"
                        )
                    },

                    dismissButton = {

                        TextButton(
                            onClick = {
                                showStopDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    },

                    confirmButton = {

                        TextButton(
                            onClick = {

                                val userId = currentUser?.uid ?: ""

                                dbHelper.stopTrackingSession(userId, sessionId)
                                val intent = Intent(context, LocationTrackingService::class.java)
                                context.stopService(intent)
                                isTracking = false
                                showStopDialog = false
                            }
                        ) {

                            Text(
                                text = "Stop Tracking",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
            if (showLocationSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showLocationSheet = false
                    }
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {

                        Text(
                            text = "📍 Current Location",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when {
                                locationText == "Fetching location..." ->
                                    "Fetching your current location..."

                                currentAddress.isNotBlank() ->
                                    currentAddress

                                else ->
                                    locationText
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🟢 Location Active",
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        GradientButton(
                            text = "Refresh",
                            onClick = {
                                fetchLocation()
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {

                                val uri = Uri.parse(
                                    "geo:$currentLat,$currentLng?q=$currentLat,$currentLng"
                                )

                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    uri
                                )

                                intent.setPackage(
                                    "com.google.android.apps.maps"
                                )

                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text("Open Google Maps")
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun HeaderSection(
    userName: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Column {
                val displayName =
                    userName.split(" ").firstOrNull() ?: ""
                Text(
                    text = if(userName.isNotBlank())
                        "Hi $displayName,"
                        else "Hi 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary

                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Stay Safe,We're here for you",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onMenuClick: () -> Unit
) {
    CenterAlignedTopAppBar(

        title = {
            Text(
                text = "HER BEACON",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },

        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
