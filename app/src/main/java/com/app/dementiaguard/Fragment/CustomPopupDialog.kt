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

class CustomPopupDialog(
    private val header: String,
    private val body: String,
    private val onConfirm: (() -> Unit)? = null
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_custom_popup, container, false)

        val txtHeader: TextView = view.findViewById(R.id.txtPopupHeader)
        val txtBody: TextView = view.findViewById(R.id.txtPopupBody)
        val btnOk: Button = view.findViewById(R.id.btnOkDialog)
        val btnCancel: Button = view.findViewById(R.id.btnCancelDialog)

        txtHeader.text = header
        txtBody.text = body

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnOk.setOnClickListener {
            onConfirm?.invoke()  // Call the function passed from Activity
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
