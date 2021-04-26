package com.amati.deus.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.amati.deus.Constants

class SharedPreferencesManager(val context: Context) {

    private val mainKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private fun getEncryptedSharedPreferences(): SharedPreferences {

        return EncryptedSharedPreferences.create(
            Constants.ENCRYPTED_PREFERENCES_FILE_NAME,
            mainKey,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSavedVersionCode(): Int {
        val DOESNT_EXIST = -1
        val prefs: SharedPreferences = getEncryptedSharedPreferences()
        return prefs.getInt(Constants.PREFERENCES_VERSION_CODE_KEY, DOESNT_EXIST)
    }

    fun setSavedVersionCode(versionCode: Int) {
        val prefs: SharedPreferences = getEncryptedSharedPreferences()
        with(prefs.edit()) {
            putInt(Constants.PREFERENCES_VERSION_CODE_KEY, versionCode)
            apply()
        }
    }

    fun isFirstTimeLaunch(): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(Constants.FIRST_TIME_LAUNCH, true)
    }

    fun setFirstTimeLaunch(value: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(Constants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(Constants.FIRST_TIME_LAUNCH, value)
            apply()
        }
    }


}