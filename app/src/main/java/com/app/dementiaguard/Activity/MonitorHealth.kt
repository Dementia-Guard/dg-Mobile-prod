package com.app.dementiaguard.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.dementiaguard.Model.MyData
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.StatusBarUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MonitorHealth : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference

    // TextView references
    private lateinit var tvO2: TextView
    private lateinit var tvSos: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvSteps: TextView
    private lateinit var tvPulse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_monitor_health)

        // Handle insets differently to achieve full screen
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Exclude bottom padding
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)

        // Initialize TextViews
        initializeViews()

        // Initialize Firebase
        setupFirebase()

        // Start listening for data changes
        listenForRealtimeUpdates()
    }
    private fun initializeViews() {
        tvO2 = findViewById(R.id.tvMHO2)
        tvSos = findViewById(R.id.tvMHSos)
        tvTemperature = findViewById(R.id.tvMHTemperature)
        tvSteps = findViewById(R.id.tvMHSteps)
        tvPulse = findViewById(R.id.tvMHPulseReal)
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("sensorData/5345bgesb4w534/data") // Use the same path as in TrackUser
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
        // Update UI components with the real-time health data
        tvTemperature.text = data.temperature
        tvPulse.text = data.pulseRate.toString()
        tvSteps.text = data.stepCount.toString()
        tvSos.text = if (data.isSOS) "ACTIVE" else "INACTIVE"
        tvO2.text = data.bloodOxygen
        // Assuming oxygen level and SOS status are also in your MyData class
        // If they're not, you'll need to modify your MyData class to include them
//        if (data.bloodOxygen != null) {
//
//        }
//
//        if (data.isSos != null) {
//
//        }
    }
}