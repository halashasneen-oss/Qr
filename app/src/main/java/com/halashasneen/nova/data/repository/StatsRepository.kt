package com.halashasneen.nova.data.repository

import com.halashasneen.nova.data.model.AppStats
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.data.storage.JsonStorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** يحسب/يحدّث إحصائيات الاستخدام بناءً على السجل الفعلي بدل تخزين مكرر */
class StatsRepository(
    private val storage: JsonStorageHelper,
    private val historyRepository: HistoryRepository,
    private val vaultRepository: VaultRepository
) {
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun computeStats(): AppStats {
        val history = historyRepository.getAll()
        val scans = history.count { !it.isGenerated }
        val generated = history.count { it.isGenerated }
        val mostUsed = history.groupingBy { it.type }.eachCount().maxByOrNull { it.value }?.key
        val vaultCount = vaultRepository.count()
        val weekly = history
            .filter { it.timestamp >= System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
            .groupingBy { dayFormat.format(Date(it.timestamp)) }
            .eachCount()

        return AppStats(
            totalScans = scans,
            totalGenerated = generated,
            mostUsedType = mostUsed,
            vaultItemsCount = vaultCount,
            weeklyUsage = weekly
        )
    }
}
