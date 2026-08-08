package com.halashasneen.nova.data.model

/** إحصائيات استخدام التطبيق */
data class AppStats(
    val totalScans: Int = 0,
    val totalGenerated: Int = 0,
    val mostUsedType: QRType? = null,
    val vaultItemsCount: Int = 0,
    val weeklyUsage: Map<String, Int> = emptyMap()
)
