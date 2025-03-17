package com.app.dementiaguard.Fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.app.dementiaguard.Api.PreferenceApiService
import com.app.dementiaguard.Api.RetrofitClient
import com.app.dementiaguard.Model.PreferenceRequest
import com.app.dementiaguard.Model.PreferenceResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreferencesFragment : Fragment() {
    
    private lateinit var etHobby: EditText
    private lateinit var etFavoriteColor: EditText
    private lateinit var etFavoriteFood: EditText
    private lateinit var etFavoriteSong: EditText
    private lateinit var etFavoriteMovie: EditText
    private lateinit var btnSubmit: MaterialButton
    
    private val apiService = RetrofitClient.instance.create(PreferenceApiService::class.java)
    private lateinit var formCompletionManager: FormCompletionManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_preference, container, false)
        
        etHobby = view.findViewById(R.id.etHobby)
        etFavoriteColor = view.findViewById(R.id.etFavoriteColor)
        etFavoriteFood = view.findViewById(R.id.etFavoriteFood)
        etFavoriteSong = view.findViewById(R.id.etFavoriteSong)
        etFavoriteMovie = view.findViewById(R.id.etFavoriteMovie)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        
        formCompletionManager = FormCompletionManager.getInstance(requireContext())
        
        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                sendPreferencesToApi()
            }
        }
        
        return view
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (TextUtils.isEmpty(etHobby.text)) {
            etHobby.error = "Please enter your hobby"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etFavoriteColor.text)) {
            etFavoriteColor.error = "Please enter your favorite color"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etFavoriteFood.text)) {
            etFavoriteFood.error = "Please enter your favorite food"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etFavoriteSong.text)) {
            etFavoriteSong.error = "Please enter your favorite song"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etFavoriteMovie.text)) {
            etFavoriteMovie.error = "Please enter your favorite movie"
            isValid = false
        }
        
        return isValid
    }
    
    private fun sendPreferencesToApi() {
        btnSubmit.isEnabled = false
        
        val preferenceRequest = PreferenceRequest(
            user_id = 1,
            hobby = etHobby.text.toString(),
            favorite_color = etFavoriteColor.text.toString(),
            favorite_food = etFavoriteFood.text.toString(),
            favorite_song = etFavoriteSong.text.toString(),
            favorite_movie = etFavoriteMovie.text.toString()
        )
        
        apiService.createPreference(preferenceRequest).enqueue(object : Callback<PreferenceResponse> {
            override fun onResponse(call: Call<PreferenceResponse>, response: Response<PreferenceResponse>) {
                btnSubmit.isEnabled = true
                
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "Preferences saved successfully!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    
                    formCompletionManager.markPreferencesFormCompleted()
                    
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                btnSubmit.isEnabled = true
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
