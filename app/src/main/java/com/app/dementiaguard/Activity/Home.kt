package com.app.dementiaguard.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.app.dementiaguard.Fragment.ActivitiesFragment
import com.app.dementiaguard.Fragment.AssistantFragment
import com.app.dementiaguard.Fragment.DeviceFragment
import com.app.dementiaguard.Fragment.IndexFragment
import com.app.dementiaguard.Fragment.ProfileFragment
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.StatusBarUtil
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    private var bottomNavigationView: BottomNavigationView? = null
    // Define constants for resource IDs
    private val HOME_ID: Int = R.id.home
    private val DEVICE_ID: Int = R.id.device
    private val ASSISTANT_ID: Int = R.id.assistant
    private val ACTIVITIES_ID: Int = R.id.activities
    private val PROFILE_ID: Int = R.id.profile


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
//        ViewCompat.setOnApplyWindowInsetsListener(
//            findViewById(R.id.main)
//        ) { v: View, insets: WindowInsetsCompat ->
//            val systemBars =
//                insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Exclude bottom padding
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)

        //        Toast.makeText(this,apiKey,Toast.LENGTH_LONG).show();
        replaceFrag(IndexFragment())

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.profile -> replaceFrag(ProfileFragment())
                R.id.home -> replaceFrag(IndexFragment())
                R.id.activities -> replaceFrag(ActivitiesFragment())
                R.id.assistant -> replaceFrag(AssistantFragment())
                R.id.device -> replaceFrag(DeviceFragment())
                else -> return@setOnItemSelectedListener false // Handle unexpected cases
            }
            true
        }
    }
    private fun replaceFrag(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()
        bundle.putSerializable("user", "ds")
        fragment.arguments = bundle

        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}