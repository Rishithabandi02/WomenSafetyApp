package com.example.safeher.data.firebase

import com.example.safeher.data.model.Contact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RealtimeDatabaseHelper {

    private val database = FirebaseDatabase.getInstance()
    val db = database.reference
    fun saveUserProfile(name: String, email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if(userId.isEmpty()){
            onFailure("User  not Logged in")
            return
        }

        val userMap = mapOf(
            "name" to name,
            "email" to email
        )

        db.child("users")
            .child(userId)
            .setValue(userMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to save profile") }
    }

    fun getUserProfile(onResult : (name: String, email: String) -> Unit, onFailure: (String) -> Unit){
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if(currentUid.isEmpty()){
            onFailure("User not logged in")
            return
        }

        db.child("users")
            .child(currentUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val email = snapshot.child("email") .getValue(String::class.java) ?: ""
                onResult(name,email)

            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to fetch profile") }
    }

    fun addContact(name: String, phone: String, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if(userId.isEmpty()){
            onFailure("User not logged in")
            return
        }

        val contactId = db.push().key
        if (contactId == null) {
            onFailure("Failed to generate contact ID")
            return
        }
        val contactMap = mapOf(
            "name" to name,
            "phone" to phone
        )

        db.child("users")
            .child(userId)
            .child("contacts")
            .child(contactId)
            .setValue(contactMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {  e-> onFailure(e.message ?: "Failed to add contact") }
    }

    fun getContacts(onResult: (List<Contact>) -> Unit, onFailure: (String) -> Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if(userId.isEmpty()){
            onFailure("user not logged in")
            return
        }
        db.child("users")
            .child(userId)
            .child("contacts")
            .get()
            .addOnSuccessListener { snapshot ->
                val contacts = mutableListOf<Contact>()
                for(child in snapshot.children){
                    val id = child.key ?: continue
                    val name = child.child("name").value as? String?: ""
                    val phone = child.child("phone").value as? String ?: ""
                    contacts.add(Contact(id,name, phone))

                }
                onResult(contacts)

            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to fetch contacts") }
    }

    fun deleteContact(contactId : String, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.child("users")
            .child(userId)
            .child("contacts")
            .child(contactId)
            .removeValue()
            .addOnSuccessListener {  onSuccess() }
            .addOnFailureListener {  e -> onFailure(e.message ?: "Failed to delete contact") }
    }

    fun saveTrackingSession(userId: String, sessionId: String){
        val startTime = System.currentTimeMillis()
        val expiresAt = startTime + (30*60*1000) //30 minutes

        val sessionData = mapOf(
            "startTime" to startTime,
            "isActive" to true,
            "expiresAt" to expiresAt
        )

        db.child("tracking")
            .child(userId)
            .child(sessionId)
            .setValue(sessionData)
        // NEW
        db.child("tracking")
            .child(userId)
            .child("activeSessionId")
            .setValue(sessionId)

    }

    //called every 3 seconds - uploads new coordinates
    fun updateTrackingLocation(userId : String, sessionID: String, lat: Double, lng: Double){
    val timestamp = System.currentTimeMillis()
        val locationData = mapOf(
            "lat" to lat,
            "lng" to lng,
            "timestamp" to timestamp
        )

        db.child("tracking")
            .child(userId)
            .child(sessionID)
            .child("locations")
            .child(timestamp.toString())
            .setValue(locationData)

        // Also update lastUpdated at session level
        db.child("tracking")
            .child(userId)
            .child(sessionID)
            .child("lastUpdated")
            .setValue(timestamp)

    }
    // 3. Called when SOS stops (manual or auto-expiry)
    fun stopTrackingSession(userId: String, sessionId: String){
        db.child("tracking")
            .child(userId)
            .child(sessionId)
            .child("isActive")
            .setValue(false)

        db.child("tracking")
            .child(userId)
            .child("activeSessionId")
            .removeValue()
    }


    fun observeActiveSession(
        userId: String,
        onChanged: (String?) -> Unit
    ) {

        db.child("tracking")
            .child(userId)
            .child("activeSessionId")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    onChanged(snapshot.getValue(String::class.java))
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


}