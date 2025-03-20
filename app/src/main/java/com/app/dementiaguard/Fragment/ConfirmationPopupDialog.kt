package com.app.dementiaguard.Fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.dementiaguard.R

class ConfirmationPopupDialog(private val header: String, private val body: String) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_confirmation_popup, container, false)

        val txtHeader: TextView = view.findViewById(R.id.txtPopupHeader)
        val txtBody: TextView = view.findViewById(R.id.txtPopupBody)
        val btnOk: Button = view.findViewById(R.id.btnPopupOk)

        txtHeader.text = header
        txtBody.text = body

        btnOk.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}