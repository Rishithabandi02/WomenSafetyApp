package com.example.safeher.utils

import android.content.Context

class ThemePreferences(context : Context){

    private val prefs = context.getSharedPreferences(
        "theme_prefs",
        Context.MODE_PRIVATE
    )

    fun isDarkMode() : Boolean{
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled : Boolean){
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }
}