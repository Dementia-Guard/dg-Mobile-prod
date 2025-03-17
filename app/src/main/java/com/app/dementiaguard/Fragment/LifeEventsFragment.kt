package com.app.dementiaguard.Fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.app.dementiaguard.Api.LifeEventApiService
import com.app.dementiaguard.Api.RetrofitClient
import com.app.dementiaguard.Model.LifeEventRequest
import com.app.dementiaguard.Model.LifeEventResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LifeEventsFragment : Fragment() {
    
    private lateinit var etEventTitle: EditText
    private lateinit var etDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var emotionsChipGroup: ChipGroup
    private lateinit var btnSubmit: MaterialButton
    
    private val apiService = RetrofitClient.instance.create(LifeEventApiService::class.java)
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val emotionsList = listOf("Happy", "Sad", "Excited", "Proud", "Anxious", "Relaxed", "Nostalgic", "Loved")
    private lateinit var formCompletionManager: FormCompletionManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_life_events, container, false)
        
        etEventTitle = view.findViewById(R.id.etEventTitle)
        etDate = view.findViewById(R.id.etDate)
        etDescription = view.findViewById(R.id.etDescription)
        emotionsChipGroup = view.findViewById(R.id.emotionsChipGroup)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        
        formCompletionManager = FormCompletionManager.getInstance(requireContext())
        
        setupDatePicker()
        setupEmotionsChips()
        
        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                sendLifeEventToApi()
            }
        }
        
        return view
    }
    
    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        
        etDate.setOnClickListener {
            context?.let {
                DatePickerDialog(
                    it,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        
        etDate.isFocusable = false
        etDate.isClickable = true
    }
    
    private fun setupEmotionsChips() {
        emotionsList.forEach { emotion ->
            val chip = Chip(requireContext())
            chip.text = emotion
            chip.isCheckable = true
            chip.setChipBackgroundColorResource(R.color.chip_background_selector)
            chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_selector))
            emotionsChipGroup.addView(chip)
        }
    }
    
    private fun getSelectedEmotions(): List<String> {
        val selectedEmotions = mutableListOf<String>()
        for (i in 0 until emotionsChipGroup.childCount) {
            val chip = emotionsChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedEmotions.add(chip.text.toString())
            }
        }
        return selectedEmotions
    }
    
    private fun updateDateInView() {
        etDate.setText(dateFormat.format(calendar.time))
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (TextUtils.isEmpty(etEventTitle.text)) {
            etEventTitle.error = "Please enter event title"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etDate.text)) {
            etDate.error = "Please enter date"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etDescription.text)) {
            etDescription.error = "Please enter description"
            isValid = false
        }
        
        return isValid
    }
    
    private fun sendLifeEventToApi() {
        btnSubmit.isEnabled = false
        
        val lifeEventRequest = LifeEventRequest(
            user_id = 1,
            event_title = etEventTitle.text.toString(),
            event_date = etDate.text.toString(),
            description = etDescription.text.toString(),
            emotions = getSelectedEmotions()
        )
        
        apiService.createLifeEvent(lifeEventRequest).enqueue(object : Callback<LifeEventResponse> {
            override fun onResponse(call: Call<LifeEventResponse>, response: Response<LifeEventResponse>) {
                btnSubmit.isEnabled = true
                
                if (response.isSuccessful) {
                    val eventId = response.body()?.event_id ?: 0
                    val message = response.body()?.message ?: "Life event saved successfully!"
                    Toast.makeText(context, "$message (ID: $eventId)", Toast.LENGTH_SHORT).show()
                    
                    formCompletionManager.markLifeEventsFormCompleted()
                    
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<LifeEventResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
