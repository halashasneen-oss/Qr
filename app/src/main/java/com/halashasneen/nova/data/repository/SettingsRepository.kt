package com.halashasneen.nova.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.halashasneen.nova.data.model.AppSettings

/**
 * إعدادات المستخدم تُخزّن عبر SharedPreferences (سريعة وخفيفة، مناسبة لبيانات بسيطة كهذه)
 * بدلاً من JSON لأنها قيم صغيرة تُقرأ باستمرار.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("qrpro_settings", Context.MODE_PRIVATE)

    fun getSettings(): AppSettings = AppSettings(
        language = prefs.getString(KEY_LANGUAGE, "ar") ?: "ar",
        darkMode = prefs.getString(KEY_DARK_MODE, "system") ?: "system",
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
        scanSoundEnabled = prefs.getBoolean(KEY_SCAN_SOUND, true),
        autoCopyEnabled = prefs.getBoolean(KEY_AUTO_COPY, false),
        saveHistoryEnabled = prefs.getBoolean(KEY_SAVE_HISTORY, true),
        privacyModeEnabled = prefs.getBoolean(KEY_PRIVACY_MODE, false),
        vaultPinHash = prefs.getString(KEY_VAULT_PIN, null),
        vaultBiometricEnabled = prefs.getBoolean(KEY_VAULT_BIOMETRIC, false)
    )

    fun saveSettings(settings: AppSettings) {
        prefs.edit {
            putString(KEY_LANGUAGE, settings.language)
            putString(KEY_DARK_MODE, settings.darkMode)
            putBoolean(KEY_VIBRATION, settings.vibrationEnabled)
            putBoolean(KEY_SCAN_SOUND, settings.scanSoundEnabled)
            putBoolean(KEY_AUTO_COPY, settings.autoCopyEnabled)
            putBoolean(KEY_SAVE_HISTORY, settings.saveHistoryEnabled)
            putBoolean(KEY_PRIVACY_MODE, settings.privacyModeEnabled)
            putString(KEY_VAULT_PIN, settings.vaultPinHash)
            putBoolean(KEY_VAULT_BIOMETRIC, settings.vaultBiometricEnabled)
        }
    }

    fun setVaultPin(pinHash: String) = prefs.edit { putString(KEY_VAULT_PIN, pinHash) }

    fun hasVaultPin(): Boolean = prefs.getString(KEY_VAULT_PIN, null) != null

    /** يفعّل وضع بلا إعلانات لمدة ساعة بعد مشاهدة إعلان مكافأة */
    fun grantAdFreeForOneHour() {
        val expiryTime = System.currentTimeMillis() + (60 * 60 * 1000)
        prefs.edit { putLong(KEY_AD_FREE_UNTIL, expiryTime) }
    }

    /** يتحقق إذا كان المستخدم لسا داخل فترة بلا إعلانات */
    fun isAdFreeActive(): Boolean {
        val expiryTime = prefs.getLong(KEY_AD_FREE_UNTIL, 0L)
        return System.currentTimeMillis() < expiryTime
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_VIBRATION = "vibration"
        private const val KEY_SCAN_SOUND = "scan_sound"
        private const val KEY_AUTO_COPY = "auto_copy"
        private const val KEY_SAVE_HISTORY = "save_history"
        private const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val KEY_VAULT_PIN = "vault_pin"
        private const val KEY_VAULT_BIOMETRIC = "vault_biometric"
        private const val KEY_AD_FREE_UNTIL = "ad_free_until"
    }
}
