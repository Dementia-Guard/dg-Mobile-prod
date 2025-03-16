package com.app.dementiaguard.Activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.dementiaguard.Model.MyData
import com.app.dementiaguard.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TrackUser : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var mGoogleMap: GoogleMap
    private var lastKnownLocation: LatLng? = null // To hold the most recent coordinates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("sensorData/5345bgesb4w534/data") // Replace with your actual path

        // Initialize the map fragment
        val mapFrag = supportFragmentManager.findFragmentById(R.id.gmapVeiwer) as SupportMapFragment
        mapFrag.getMapAsync(this)

        // Start listening for data changes
        listenForRealtimeUpdates()
    }
    private fun listenForRealtimeUpdates() {
        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Parse data snapshot to a data class
                val data = snapshot.getValue(MyData::class.java)
                data?.let {
                    updateUI(it)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read data: ${error.message}")
            }
        })
    }
    private fun updateUI(data: MyData) {
        // Update UI components with the real-time data
        findViewById<TextView>(R.id.temperatureTextView).text = "Temperature: ${data.temperature}"
        findViewById<TextView>(R.id.latitudeTextView).text = "Latitude: ${data.location.latitude}"
        findViewById<TextView>(R.id.longitudeTextView).text = "Longitude: ${data.location.longitude}"
        findViewById<TextView>(R.id.pulseRateTextView).text = "Pulse Rate: ${data.pulseRate}"
        findViewById<TextView>(R.id.stepCountTextView).text = "Step Count: ${data.stepCount}"

        // Update map with the new location
        val latitude = data.location.latitude.toDoubleOrNull()
        val longitude = data.location.longitude.toDoubleOrNull()
        if (latitude != null && longitude != null) {
            val newLocation = LatLng(latitude, longitude)
            lastKnownLocation = newLocation // Save the updated location
            updateMapLocation(newLocation)
        }
    }
    private fun updateMapLocation(location: LatLng) {
        if (::mGoogleMap.isInitialized) {
            mGoogleMap.clear() // Clear old markers
            mGoogleMap.addMarker(
                MarkerOptions().position(location).title("Current Location")
            )
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0
    }
}