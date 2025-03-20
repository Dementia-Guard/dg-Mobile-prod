package com.app.dementiaguard.Activity

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.app.dementiaguard.Model.MapAddress
import com.app.dementiaguard.Model.MyData
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.StatusBarUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class TrackUser : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference
    private lateinit var mGoogleMap: GoogleMap
    private var lastKnownLocation: LatLng? = null // To hold the most recent coordinates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContentView(R.layout.activity_track_user)

        // Handle insets differently to achieve full screen
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            // Don't apply any padding to achieve full screen effect
            v.setPadding(0, 0, 0, 0)
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)
        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("sensorData/5345bgesb4w534/data") // Replace with your actual path


        // Initialize the bottom sheet behavior
        val bottomSheet = findViewById<NestedScrollView>(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Set initial state to be half-expanded
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        // Or if you prefer to set a specific expanded ratio (e.g., 50%)
//        bottomSheetBehavior.halfExpandedRatio = 0.4f

        // Optional: Set callbacks for state changes
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // React to state changes
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // React to dragging events
            }
        })

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
        findViewById<TextView>(R.id.tvTrackUserTemperature).text = data.temperature
        findViewById<TextView>(R.id.tvTrackUserLats).text = data.location.latitude
        findViewById<TextView>(R.id.tvTrackUserLongs).text = data.location.longitude
        findViewById<TextView>(R.id.tvTrackUserPulseRate).text = data.pulseRate.toString()
        findViewById<TextView>(R.id.tvTrackUserStepCount).text = data.stepCount.toString()

        // Update map with the new location
        val latitude = data.location.latitude.toDoubleOrNull()
        val longitude = data.location.longitude.toDoubleOrNull()
        if (latitude != null && longitude != null) {
            val newLocation = LatLng(latitude, longitude)
            lastKnownLocation = newLocation // Save the updated location
            updateMapLocation(newLocation)

//            // Fetch place name using Geocoder
            val placeName = getPlaceName(latitude, longitude)
            findViewById<TextView>(R.id.tvTrackUserAddressFull).text = placeName
            // Fetch place name components
            val addressComponents = getAddressComponents(latitude, longitude)

            // Display different parts of the address in the UI
//            findViewById<TextView>(R.id.cityTextView).text = "City: ${addressComponents.city}"
//            findViewById<TextView>(R.id.streetTextView).text = "Street: ${addressComponents.street}"
            findViewById<TextView>(R.id.tvTrackUserState).text = addressComponents.state
            findViewById<TextView>(R.id.tvTrackUserCountry).text = addressComponents.country
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
    private fun getAddressComponents(latitude: Double, longitude: Double): MapAddress {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: "Unknown City"
                    val street = address.thoroughfare ?: "Unknown Street"
                    val state = address.adminArea ?: "Unknown State"
                    val country = address.countryName ?: "Unknown Country"
                    return MapAddress(city, street, state, country)
                }
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Failed to get address components", e)
        }
        return MapAddress("N/A", "N/A", "N/A", "N/A")
    }

    override fun onMapReady(p0: GoogleMap) {
        mGoogleMap = p0
    }
    private fun getPlaceName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses.get(0)
                    // You can customize which part of the address to use, here we use the full address.
                    return address.getAddressLine(0) ?: "Unknown Place"
                }
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Failed to get place name", e)
        }
        return "Unknown Place"
    }
}