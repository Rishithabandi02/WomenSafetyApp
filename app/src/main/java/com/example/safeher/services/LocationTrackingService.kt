package com.example.safeher.services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.Manifest
import androidx.core.app.NotificationManagerCompat
import com.example.safeher.data.firebase.RealtimeDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationTrackingService  :  Service(){

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var userId: String = ""
    private var sessionId: String = ""
    private var startTime : Long = 0L

    private val dbHelper = RealtimeDatabaseHelper()

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    companion object{
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "location_tracking_channel"
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userId = intent?.getStringExtra(EXTRA_USER_ID) ?: ""
        sessionId = intent?.getStringExtra(EXTRA_SESSION_ID) ?: ""

        startTime = System.currentTimeMillis()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(
            this, CHANNEL_ID
        )
            .setContentTitle("HerBeacon is active")
            .setContentText("Your location is being tracked for your safety")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // Makes notification non-dismissable
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startLocationUpdates()
        serviceScope.launch {
            delay(30 * 60 * 1000L)
            stopTrackingAutomatically()
        }

        return START_STICKY

    }





    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startLocationUpdates(){
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
        3000L
        ).build()

        locationCallback = object  : LocationCallback(){
            override fun onLocationResult(p0 : LocationResult) {
                super.onLocationResult(p0)
                val location = p0.lastLocation ?: return

                val lat = location.latitude
                val lng = location.longitude
                if(userId.isNotEmpty() && sessionId.isNotEmpty()){
                    // Auto-stop after 30 minutes
                    val elapsed = System.currentTimeMillis() - startTime
                    if(elapsed >= 30 * 60 * 1000){
                        dbHelper.stopTrackingSession(userId, sessionId)
                        return
                    }
                    dbHelper.updateTrackingLocation(userId,sessionId,lat,lng)


                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            fusedClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }



    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)

    }
    private fun stopTrackingAutomatically() {
        if (userId.isEmpty() || sessionId.isEmpty()) {
            return
        }
        dbHelper.stopTrackingSession(userId, sessionId)
        showAutoStopNotification()
        stopSelf()
    }
    private fun showAutoStopNotification() {

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("HER BEACON")
            .setContentText("Live location sharing has automatically stopped after 30 minutes.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(999, notification)
    }


}