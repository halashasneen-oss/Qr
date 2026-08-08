package com.halashasneen.nova.data.model

/** إعدادات المستخدم المحفوظة */
data class AppSettings(
    val language: String = "ar",
    val darkMode: String = "system",
    val vibrationEnabled: Boolean = true,
    val scanSoundEnabled: Boolean = true,
    val autoCopyEnabled: Boolean = false,
    val saveHistoryEnabled: Boolean = true,
    val privacyModeEnabled: Boolean = false,
    val vaultPinHash: String? = null,
    val vaultBiometricEnabled: Boolean = false
)
