package com.myspsgame.app

import android.content.Context

class SharedPrefsUtils {

    companion object {

        val PREFS: String = "SharedPrefsUtils"

        fun putString(key: String, value: String, context: Context) {
            val sharedPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun getString(key: String, context: Context): String? {
            val sharedPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            return sharedPrefs.getString(key, "")
        }
    }

}