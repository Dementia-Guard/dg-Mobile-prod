package com.app.dementiaguard.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.google.android.material.card.MaterialCardView

class IndexFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_index, container, false)

        // Find the MaterialCardViews by their IDs
        val cvTherapyAssist = view.findViewById<MaterialCardView>(R.id.therapyAssist)
        val cvActivities = view.findViewById<MaterialCardView>(R.id.caIndexCard)
        val cvDevice = view.findViewById<MaterialCardView>(R.id.cvWearMonitorHealth)

        // Set click listener for therapyAssist card
        cvTherapyAssist.setOnClickListener {
            val formCompletionManager = FormCompletionManager.getInstance(requireContext())
            if (formCompletionManager.areAllFormsCompleted()) {
                replaceFrag(AssistantFragment())
            }
        }

        // Set click listener for caIndexCard
        cvActivities.setOnClickListener {
            replaceFrag(ActivitiesFragment())
        }

        // Set click listener for cvWearMonitorHealth
        cvDevice.setOnClickListener {
            replaceFrag(DeviceFragment())
        }

        return view
    }

    private fun replaceFrag(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}