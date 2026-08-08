package com.halashasneen.nova.data.model

import java.util.UUID

/** عنصر محمي داخل الـ Vault (Wi-Fi، بطاقة عمل، رابط حساس، كود مهم...) */
data class VaultItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: QRType,
    val content: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
