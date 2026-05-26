package com.aistudio.sharmakhata.pqmzvk.util

import android.content.Context

object SessionManager {
    private const val PREFS = "grahbook_session"
    private const val KEY_TOKEN = "token"

    @Volatile
    var token: String? = null
        private set

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        token = prefs.getString(KEY_TOKEN, null)
    }

    fun setToken(context: Context, value: String?) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, value).apply()
        token = value
    }

    fun clear(context: Context) {
        setToken(context, null)
    }
}

