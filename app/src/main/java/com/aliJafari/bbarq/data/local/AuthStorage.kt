package com.aliJafari.bbarq.data.local

import android.content.Context
import androidx.core.content.edit

class AuthStorage(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit { putString("session_token", token) }
    }

    fun getToken(): String? {
        return prefs.getString("session_token", null)
    }

    fun clearToken() {
        prefs.edit { remove("session_token") }
    }
}