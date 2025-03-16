package com.app.dementiaguard.Fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.CardView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.app.dementiaguard.Activity.TrackUser
import com.app.dementiaguard.R
import com.google.android.material.card.MaterialCardView

class DeviceFragment : Fragment() {
    private lateinit var cvWearTrack: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.fragment_device, container, false)

        cvWearTrack = view.findViewById(R.id.cvWearTrack)
        cvWearTrack.setOnClickListener {
            startActivity(Intent(requireActivity(),TrackUser::class.java))
        }

        return  view
    }
}