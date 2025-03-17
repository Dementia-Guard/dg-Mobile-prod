package com.app.dementiaguard.Utils

import android.content.Context
import android.content.SharedPreferences

class FormCompletionManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "form_completion_prefs"
        private const val PREFERENCES_FORM_COMPLETED = "preferences_form_completed"
        private const val LIFE_EVENTS_FORM_COMPLETED = "life_events_form_completed"
        private const val IMAGES_FORM_COMPLETED = "images_form_completed"
        
        @Volatile
        private var instance: FormCompletionManager? = null
        
        fun getInstance(context: Context): FormCompletionManager {
            return instance ?: synchronized(this) {
                instance ?: FormCompletionManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    fun markPreferencesFormCompleted() {
        sharedPreferences.edit().putBoolean(PREFERENCES_FORM_COMPLETED, true).apply()
    }
    
    fun markLifeEventsFormCompleted() {
        sharedPreferences.edit().putBoolean(LIFE_EVENTS_FORM_COMPLETED, true).apply()
    }
    
    fun markImagesFormCompleted() {
        sharedPreferences.edit().putBoolean(IMAGES_FORM_COMPLETED, true).apply()
    }
    
    fun resetAllFormCompletionStatus() {
        sharedPreferences.edit()
            .putBoolean(PREFERENCES_FORM_COMPLETED, false)
            .putBoolean(LIFE_EVENTS_FORM_COMPLETED, false)
            .putBoolean(IMAGES_FORM_COMPLETED, false)
            .apply()
    }
    
    fun areAllFormsCompleted(): Boolean {
        return isPreferencesFormCompleted() && 
               isLifeEventsFormCompleted() && 
               isImagesFormCompleted()
    }
    
    fun isPreferencesFormCompleted(): Boolean {
        return sharedPreferences.getBoolean(PREFERENCES_FORM_COMPLETED, false)
    }
    
    fun isLifeEventsFormCompleted(): Boolean {
        return sharedPreferences.getBoolean(LIFE_EVENTS_FORM_COMPLETED, false)
    }
    
    fun isImagesFormCompleted(): Boolean {
        return sharedPreferences.getBoolean(IMAGES_FORM_COMPLETED, false)
    }
    
    fun getIncompleteFormsList(): List<String> {
        val incompleteFormsList = mutableListOf<String>()
        
        if (!isPreferencesFormCompleted()) {
            incompleteFormsList.add("Personal Preferences")
        }
        
        if (!isLifeEventsFormCompleted()) {
            incompleteFormsList.add("Life Events")
        }
        
        if (!isImagesFormCompleted()) {
            incompleteFormsList.add("Images")
        }
        
        return incompleteFormsList
    }
}
