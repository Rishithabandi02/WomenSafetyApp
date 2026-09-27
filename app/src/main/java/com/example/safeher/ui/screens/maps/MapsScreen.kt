package com.example.safeher.ui.screens.maps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.maps.android.compose.*

@Composable
fun MapsScreen(userId : String, sessionId: String){

    //All location points collected so far
    val locationPoints = remember { mutableStateListOf<LatLng>() }

    //Current position of the marker
    var currentPosition by remember { mutableStateOf<LatLng?>(null) }

    //Camera position state
    val cameraPositionState = rememberCameraPositionState()

    var isMapLoaded by remember { mutableStateOf(false) }

    // Listen to Firebase for live location updates
    LaunchedEffect(sessionId) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("tracking")
            .child(userId)
            .child(sessionId)
            .child("locations")
            .addChildEventListener(
                object  : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val lat = snapshot.child("lat").getValue(Double ::class.java) ?: return
                    val lng = snapshot.child("lng").getValue(Double::class.java) ?: return
                    val newPoint = LatLng(lat, lng)

                    locationPoints.add(newPoint)
                    currentPosition = newPoint

                    //Move Camera to latest position
                    //Only move camera if map is ready - otherwise - app crash
                    if(isMapLoaded) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(newPoint, 17f)
                        )
                    }


                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }
    if(currentPosition == null) {
        // Show loading until first location arrives
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
            Text(
                text = "Waiting for location...",
                modifier = Modifier.padding(top = 80.dp)
            )
        }
    }
    else{
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                isMapLoaded = true
            }
        ){
            //Current location marker
            currentPosition?.let { pos ->
                Marker(
                    state = MarkerState(position = pos),
                    title = "You are here"
                )

            }

            //Polyline - route history
            if(locationPoints.size > 1) {
                Polyline(
                    points = locationPoints.toList(),
                    color = Color.Blue,
                    width = 8f
                )
            }
        }
    }

}