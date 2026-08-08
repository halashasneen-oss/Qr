package com.halashasneen.nova.ui.vault

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.data.model.VaultItem
import com.halashasneen.nova.data.repository.SettingsRepository
import com.halashasneen.nova.data.repository.VaultRepository
import kotlinx.coroutines.launch
import java.security.MessageDigest

class VaultViewModel(
    private val repository: VaultRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _items = MutableLiveData<List<VaultItem>>()
    val items: LiveData<List<VaultItem>> = _items

    var isUnlocked: Boolean = false

    fun load() {
        viewModelScope.launch {
            _items.value = repository.getAll()
        }
    }

    fun hasPin(): Boolean = settingsRepository.hasVaultPin()

    fun isBiometricEnabled(): Boolean = settingsRepository.getSettings().vaultBiometricEnabled

    fun setPin(pin: String) {
        settingsRepository.setVaultPin(hash(pin))
    }

    fun verifyPin(pin: String): Boolean {
        val stored = settingsRepository.getSettings().vaultPinHash ?: return false
        return stored == hash(pin)
    }

    fun addItem(title: String, type: QRType, content: String, note: String) {
        viewModelScope.launch {
            repository.add(VaultItem(title = title, type = type, content = content, note = note))
            load()
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            load()
        }
    }

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
