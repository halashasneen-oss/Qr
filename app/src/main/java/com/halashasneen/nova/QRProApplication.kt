package com.halashasneen.nova

import android.app.Application
import com.halashasneen.nova.data.repository.HistoryRepository
import com.halashasneen.nova.data.repository.SettingsRepository
import com.halashasneen.nova.data.repository.StatsRepository
import com.halashasneen.nova.data.repository.VaultRepository
import com.halashasneen.nova.data.storage.JsonStorageHelper
import com.google.android.gms.ads.MobileAds

/**
 * نقطة تجميع الاعتماديات (Service Locator خفيف بدل Dependency Injection Framework،
 * حسب متطلبات المشروع بعدم استخدام Hilt/Dagger).
 * كل Repository يُبنى مرة واحدة فقط ويُعاد استخدامه من كل الشاشات.
 */
class QRProApplication : Application() {

    lateinit var storageHelper: JsonStorageHelper
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var vaultRepository: VaultRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var statsRepository: StatsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
        MobileAds.initialize(this) {}

        storageHelper = JsonStorageHelper(this)
        historyRepository = HistoryRepository(storageHelper)
        vaultRepository = VaultRepository(storageHelper)
        settingsRepository = SettingsRepository(this)
        statsRepository = StatsRepository(storageHelper, historyRepository, vaultRepository)
    }

    /** Crash Handling بسيط: يمنع تحطم التطبيق فجأة ويسجل الخطأ محليًا لأغراض التصحيح لاحقًا */
    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val logFile = java.io.File(filesDir, "crash_log.txt")
                logFile.appendText(
                    "\n---- ${java.util.Date()} ----\n" +
                        throwable.stackTraceToString()
                )
            } catch (_: Exception) {
                // لا شيء نفعله إذا فشل حتى تسجيل الخطأ
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
