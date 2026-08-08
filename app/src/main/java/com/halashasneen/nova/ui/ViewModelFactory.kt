package com.halashasneen.nova.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.ui.create.CreateViewModel
import com.halashasneen.nova.ui.history.HistoryViewModel
import com.halashasneen.nova.ui.home.HomeViewModel
import com.halashasneen.nova.ui.result.ResultViewModel
import com.halashasneen.nova.ui.settings.SettingsViewModel
import com.halashasneen.nova.ui.vault.VaultViewModel

/**
 * مصنع بسيط لإنشاء الـ ViewModels يدويًا بدل الاعتماد على Hilt،
 * ويربطها بالـ Repositories المُنشأة مرة واحدة في QRProApplication.
 */
class ViewModelFactory(private val app: QRProApplication) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(app.historyRepository, app.vaultRepository, app.statsRepository) as T

            modelClass.isAssignableFrom(ResultViewModel::class.java) ->
                ResultViewModel(app.historyRepository, app.vaultRepository, app.settingsRepository) as T

            modelClass.isAssignableFrom(CreateViewModel::class.java) ->
                CreateViewModel(app.historyRepository) as T

            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(app.historyRepository) as T

            modelClass.isAssignableFrom(VaultViewModel::class.java) ->
                VaultViewModel(app.vaultRepository, app.settingsRepository) as T

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(app.settingsRepository, app.historyRepository) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
