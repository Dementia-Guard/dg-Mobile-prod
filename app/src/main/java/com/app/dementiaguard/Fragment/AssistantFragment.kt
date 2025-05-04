package com.app.dementiaguard.Fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.google.android.material.card.MaterialCardView

class AssistantFragment : Fragment() {

    private lateinit var formCompletionManager: FormCompletionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant, container, false)

        formCompletionManager = FormCompletionManager.getInstance(requireContext())

        val personalStoriesCard = view.findViewById<MaterialCardView>(R.id.va_ps_car)

        personalStoriesCard.setOnClickListener {
            val preferencesFragment = PreferencesFragment()
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, preferencesFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        val lifeEventsCard = view.findViewById<MaterialCardView>(R.id.va_le_card)

        lifeEventsCard.setOnClickListener {
            val lifeEventsFragment = LifeEventsFragment()
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, lifeEventsFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        val imagesStoriesCard = view.findViewById<MaterialCardView>(R.id.va_ps_card)

        imagesStoriesCard.setOnClickListener {
            val imagesFragment = ImagesFragment()
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, imagesFragment)
                ?.addToBackStack(null)
                ?.commit()
        }

        val therapyAssistantCard = view.findViewById<MaterialCardView>(R.id.va_start_card)

        therapyAssistantCard.setOnClickListener {
            if (formCompletionManager.areAllFormsCompleted()) {
                val therapyAssistantFragment = TherapyAssistantFragment()
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.frame_layout, therapyAssistantFragment)
                    ?.addToBackStack(null)
                    ?.commit()
            } else {
                showIncompleteFormsDialog()
            }
        }

        return view
    }

    private fun showIncompleteFormsDialog() {
        val incompleteFormsList = formCompletionManager.getIncompleteFormsList()

        val message = StringBuilder().apply {
            append("Please complete the following forms before accessing the Therapy Assistant:\n\n")
            incompleteFormsList.forEachIndexed { index, formName ->
                append("${index + 1}. $formName\n")
            }
        }.toString()

        AlertDialog.Builder(requireContext())
            .setTitle("Forms Required")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}