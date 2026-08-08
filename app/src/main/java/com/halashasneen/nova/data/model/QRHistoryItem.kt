package com.halashasneen.nova.data.model

import java.util.UUID

/**
 * عنصر واحد داخل سجل المسح أو الإنشاء.
 * isGenerated = true إذا كان الكود تم توليده من داخل التطبيق (Create QR)
 * isGenerated = false إذا كان ناتج عن عملية مسح (Scan)
 */
data class QRHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val rawContent: String,
    val type: QRType,
    val displayTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isGenerated: Boolean = false,
    val isSecure: Boolean? = null,
    val customization: QRCustomization? = null
)
