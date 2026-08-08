package com.halashasneen.nova.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

fun Context.copyToClipboard(label: String, text: String) {
    val clipboard = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, null))
}

fun Context.vibrateShort() {
    val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = ContextCompat.getSystemService(this, VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        ContextCompat.getSystemService(this, Vibrator::class.java)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(35)
    }
}

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
