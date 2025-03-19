package com.app.dementiaguard.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.app.dementiaguard.Activity.QuestionSession
import com.app.dementiaguard.Model.UserDetailsRequest
import com.app.dementiaguard.R
import com.app.dementiaguard.Service.RetrofitService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivitiesFragment : Fragment() {

    private val retrofitService = RetrofitService()
    private lateinit var currentDifficultyLevel: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        val startCard: CardView = view.findViewById(R.id.ca_start_card)
        currentDifficultyLevel = view.findViewById(R.id.txtCurrrentDifficultyLevel)

        startCard.setOnClickListener {
            val intent = Intent(activity, QuestionSession::class.java)
            startActivity(intent)
        }

        fetchUserDetails()

        return view
    }

    private fun fetchUserDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiService = retrofitService.createApiService()
                val request = UserDetailsRequest(userId = 1)
                val response = apiService.getUserDetails(request)

                if (response.isSuccessful && response.body() != null) {
                    val userDetails = response.body()
                    val difficultyLevel = userDetails?.difficultyLevel ?: "N/A"

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        currentDifficultyLevel.text = "$difficultyLevel"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        currentDifficultyLevel.text = "Failed to load level"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentDifficultyLevel.text = "Error: ${e.message}"
                }
            }
        }
    }
}