package com.app.dementiaguard.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.google.android.material.card.MaterialCardView
import com.app.dementiaguard.Fragment.TherapyAssistantFragment

class IndexFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_index, container, false)

        val cvIndexGemmaAssist = view.findViewById<MaterialCardView>(R.id.cvWearConfDevice)

        cvIndexGemmaAssist.setOnClickListener {
            val formCompletionManager = FormCompletionManager.getInstance(requireContext())

            if (formCompletionManager.areAllFormsCompleted()) {
                replaceFrag(TherapyAssistantFragment())
            }
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