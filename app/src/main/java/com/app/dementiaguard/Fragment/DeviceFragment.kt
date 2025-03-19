package com.app.dementiaguard.Fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.CardView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.app.dementiaguard.Activity.MonitorHealth
import com.app.dementiaguard.Activity.Sos
import com.app.dementiaguard.Activity.TrackUser
import com.app.dementiaguard.Activity.Traveler
import com.app.dementiaguard.R
import com.google.android.material.card.MaterialCardView

class DeviceFragment : Fragment() {
    private lateinit var cvWearTrack: MaterialCardView
    private lateinit var cvWearSos: MaterialCardView
    private lateinit var cvWearTravel: MaterialCardView
    private lateinit var cvWearMonitorHealth: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_device, container, false)

        cvWearTrack = view.findViewById(R.id.cvWearTrack)
        cvWearSos = view.findViewById(R.id.cvWearSos)
        cvWearTravel = view.findViewById(R.id.cvWearTravel)
        cvWearMonitorHealth = view.findViewById(R.id.cvWearMonitorHealth)

        cvWearSos.setOnClickListener {
            startActivity(Intent(requireActivity(),Sos::class.java))
        }
        cvWearTrack.setOnClickListener {
            startActivity(Intent(requireActivity(),TrackUser::class.java))
        }
        cvWearTravel.setOnClickListener {
            startActivity(Intent(requireActivity(),Traveler::class.java))
        }
        cvWearMonitorHealth.setOnClickListener {
            startActivity(Intent(requireActivity(),MonitorHealth::class.java))
        }

        return  view
    }
}