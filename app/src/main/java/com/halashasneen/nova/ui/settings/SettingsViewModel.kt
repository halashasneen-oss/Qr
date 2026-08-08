package com.halashasneen.nova.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.AppSettings
import com.halashasneen.nova.data.repository.HistoryRepository
import com.halashasneen.nova.data.repository.SettingsRepository
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    fun getSettings(): AppSettings = settingsRepository.getSettings()

    fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(getSettings())
        settingsRepository.saveSettings(updated)
    }

    fun backupToFile(destination: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val history = historyRepository.getAll()
                val gson = com.google.gson.Gson()
                destination.writeText(gson.toJson(history))
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun restoreFromFile(source: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<com.halashasneen.nova.data.model.QRHistoryItem>>() {}
                val items: List<com.halashasneen.nova.data.model.QRHistoryItem> =
                    gson.fromJson(source.readText(), type.type) ?: emptyList()
                items.forEach { historyRepository.add(it) }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
