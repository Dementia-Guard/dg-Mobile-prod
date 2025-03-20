package com.app.dementiaguard.Activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.StatusBarUtil
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Sos : AppCompatActivity() {
    private lateinit var cvSosEnableBtn : MaterialCardView
    private lateinit var database: FirebaseDatabase
    private lateinit var dataRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sos)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Exclude bottom padding
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)

        // Initialize Firebase
        setupFirebase()

        cvSosEnableBtn= findViewById(R.id.cvSosEnableBtn)
        cvSosEnableBtn.setOnClickListener {
            showConfirmationDialog()
        }
    }
    private fun setupFirebase() {
        database = FirebaseDatabase.getInstance()
        dataRef = database.getReference("sensorData/5345bgesb4w534/data")
    }

    private fun showConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.botom_sheet_layout)

        // Find the confirm button in your bottom sheet layout
        // Assuming there's a button with id btnConfirmSos in your bottom sheet layout
        val confirmButton = dialog.findViewById<Button>(R.id.btnConfirm)
        val cancelButton = dialog.findViewById<Button>(R.id.btnCancel)

        confirmButton?.setOnClickListener {
            // Update the SOS status in Firebase
            updateSosStatus(true)
            dialog.dismiss()
        }

        cancelButton?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun updateSosStatus(isActive: Boolean) {
        // Update only the isSos field without affecting other data
        dataRef.child("isSOS").setValue(isActive)
            .addOnSuccessListener {
                Log.d("SOS", "SOS status updated successfully to $isActive")
                // You might want to show a toast or some UI feedback here
                // Show success toast
                Toast.makeText(
                    this@Sos,
                    "SOS alert activated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Redirect back to previous screen
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("SOS", "Failed to update SOS status", e)
                // Handle the error, maybe show a toast
                // Show error toast
                Toast.makeText(
                    this@Sos,
                    "Failed to activate SOS alert. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}