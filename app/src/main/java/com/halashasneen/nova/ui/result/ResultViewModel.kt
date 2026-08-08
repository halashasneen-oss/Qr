package com.halashasneen.nova.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.data.model.VaultItem
import com.halashasneen.nova.data.repository.HistoryRepository
import com.halashasneen.nova.data.repository.SettingsRepository
import com.halashasneen.nova.data.repository.VaultRepository
import com.halashasneen.nova.util.QRContentParser
import kotlinx.coroutines.launch

class ResultViewModel(
    private val historyRepository: HistoryRepository,
    private val vaultRepository: VaultRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    fun parse(raw: String): QRContentParser.ParsedContent = QRContentParser.parse(raw)

    /** يحفظ العنصر في السجل تلقائيًا إذا كان إعداد "حفظ السجل" مفعّل */
    fun saveToHistoryIfEnabled(item: QRHistoryItem) {
        if (!settingsRepository.getSettings().saveHistoryEnabled) return
        viewModelScope.launch {
            historyRepository.add(item)
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch { historyRepository.toggleFavorite(id) }
    }

    fun saveToVault(item: VaultItem) {
        viewModelScope.launch { vaultRepository.add(item) }
    }
}
