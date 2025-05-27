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
import com.app.dementiaguard.Model.UserDetailsResponse
import com.app.dementiaguard.R
import com.app.dementiaguard.Service.RetrofitService
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import kotlin.math.roundToInt

class ActivitiesFragment : Fragment() {

    private val retrofitService = RetrofitService()
    private lateinit var currentDifficultyLevel: TextView
    private lateinit var recentScoreTxt: TextView
    private lateinit var seeMoreText: TextView
    private var recentScore: String = "N/A"
    private var userDetails: UserDetailsResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        val startCard: CardView = view.findViewById(R.id.ca_start_card)
        val currentDifficultyLevelCard = view.findViewById<MaterialCardView>(R.id.ca_cdl_card)
        val recentScoreCard = view.findViewById<MaterialCardView>(R.id.ca_recent_score_card)
        currentDifficultyLevel = view.findViewById(R.id.txtCurrrentDifficultyLevel)
        recentScoreTxt = view.findViewById(R.id.txt_recent_score)
        seeMoreText = view.findViewById(R.id.txtCASeeMore)

        startCard.setOnClickListener {
            val intent = Intent(activity, QuestionSession::class.java)
            startActivity(intent)
        }

        currentDifficultyLevelCard.setOnClickListener {
            val popupDialog = ConfirmationPopupDialog("Current Difficulty Level", "This is your current difficulty level and your questioning sessions are generated considering this.")
            popupDialog.show(parentFragmentManager, "CustomPopupDialog")
        }

        recentScoreCard.setOnClickListener {
            val popupDialog = ConfirmationPopupDialog("Recent Score", "This is the score you achieved in your most recent questioning session:\n\n$recentScore%")
            popupDialog.show(parentFragmentManager, "CustomPopupDialog")
        }

        seeMoreText.setOnClickListener {
            val fragment = RecentActivitiesFragment()
            val bundle = Bundle()
            userDetails?.previousSessions?.let {
                bundle.putString("previous_sessions", Gson().toJson(it))
            }
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
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
                    userDetails = response.body()
                    val difficultyLevel = userDetails?.difficultyLevel ?: "N/A"
                    val avgRecentScore = userDetails?.recentAvgScore
                    val score = avgRecentScore?.times(100)?.roundToInt()
                    recentScore = (score ?: "N/A").toString()

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        currentDifficultyLevel.text = "$difficultyLevel"
                        recentScoreTxt.text = "$recentScore%"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        currentDifficultyLevel.text = "Failed to load level"
                        recentScoreTxt.text = "Failed to load score"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentDifficultyLevel.text = "Err"
                    recentScoreTxt.text = "Err"
                }
            }
        }
    }
}