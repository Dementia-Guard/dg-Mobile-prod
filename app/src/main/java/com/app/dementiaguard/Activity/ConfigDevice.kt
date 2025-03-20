package com.app.dementiaguard.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.app.dementiaguard.Model.MyData
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.StatusBarUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ConfigDevice : AppCompatActivity() {
    // Database references
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference

    // Animation views
    private lateinit var heartAnimationView: LottieAnimationView
    private lateinit var o2AnimationView: LottieAnimationView
    private lateinit var stepAnimationView: LottieAnimationView
    private lateinit var tempAnimationView: LottieAnimationView

    // Animation resources
    private val successAnimation = R.raw.online_anim // Update with your actual filename
    private val failAnimation = R.raw.ofline_anim // Update with your actual filename
    private val loadingAnimation = R.raw.pending_anim // Update with your actual filename

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config_device)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Exclude bottom padding
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)

        // Initialize animation views
        initializeViews()

        // Set initial animations to loading
        setLoadingAnimations()

        // Initialize Firebase and start listening for data
        setupFirebase()
        listenForRealtimeUpdates()
    }
    private fun initializeViews() {
        heartAnimationView = findViewById(R.id.cvSensorHeartHSAnimationView)
        o2AnimationView = findViewById(R.id.cvSensorO2HSAnimationView)
        stepAnimationView = findViewById(R.id.cvSensorStepHSAnimationView)
        tempAnimationView = findViewById(R.id.cvSensorTempHSAnimationView)
    }

    private fun setLoadingAnimations() {
        // Set all animations to loading state
        heartAnimationView.setAnimation(loadingAnimation)
        o2AnimationView.setAnimation(loadingAnimation)
        stepAnimationView.setAnimation(loadingAnimation)
        tempAnimationView.setAnimation(loadingAnimation)

        // Start all animations
        heartAnimationView.playAnimation()
        o2AnimationView.playAnimation()
        stepAnimationView.playAnimation()
        tempAnimationView.playAnimation()
    }

    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("sensorData/5345bgesb4w534/data")
    }

    private fun listenForRealtimeUpdates() {
        dataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Parse data snapshot to a data class
                val data = snapshot.getValue(MyData::class.java)
                data?.let {
                    updateSensorAnimations(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read data: ${error.message}")
                // If database reading fails, show failure animations
                setFailAnimations()
            }
        })
    }

    private fun updateSensorAnimations(data: MyData) {
        // Check if all required data fields are valid
        val isPulseValid = data.pulseRate > 0
        val isO2Valid = data.bloodOxygen.isNotEmpty()
        val isStepValid = data.stepCount >= 0
        val isTempValid = data.temperature.isNotEmpty()

        // Update heart rate animation
        updateAnimation(heartAnimationView, isPulseValid)

        // Update oxygen level animation
        updateAnimation(o2AnimationView, isO2Valid)

        // Update step count animation
        updateAnimation(stepAnimationView, isStepValid)

        // Update temperature animation
        updateAnimation(tempAnimationView, isTempValid)
    }

    private fun updateAnimation(animationView: LottieAnimationView, isValid: Boolean) {
        // Stop current animation
        if (animationView.isAnimating) {
            animationView.cancelAnimation()
        }

        // Set and play appropriate animation
        if (isValid) {
            animationView.setAnimation(successAnimation)
        } else {
            animationView.setAnimation(failAnimation)
        }

        animationView.playAnimation()
    }

    private fun setFailAnimations() {
        // Set all animations to failure state
        heartAnimationView.setAnimation(failAnimation)
        o2AnimationView.setAnimation(failAnimation)
        stepAnimationView.setAnimation(failAnimation)
        tempAnimationView.setAnimation(failAnimation)

        // Play all animations
        heartAnimationView.playAnimation()
        o2AnimationView.playAnimation()
        stepAnimationView.playAnimation()
        tempAnimationView.playAnimation()
    }
}