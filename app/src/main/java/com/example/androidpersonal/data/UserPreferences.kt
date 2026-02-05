package com.example.androidpersonal.data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_IS_DARK_MODE = "is_dark_mode"
        private const val KEY_IS_FIRST_RUN = "is_first_run"
    }

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, false) // Default to light/system (handled by logic)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()
        
    // To distinguish between "default false" and "user set to false", we might need a flag
    // but for simplicity, let's assume if it's not set, we follow system, else we override.
    // For now, let's just stick to a simple toggle that overrides system if set.
    
    fun clear() {
        prefs.edit().clear().apply()
    }
}
