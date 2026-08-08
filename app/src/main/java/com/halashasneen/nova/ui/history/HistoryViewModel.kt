package com.halashasneen.nova.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.data.repository.HistoryRepository
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _items = MutableLiveData<List<QRHistoryItem>>()
    val items: LiveData<List<QRHistoryItem>> = _items

    private var allItems: List<QRHistoryItem> = emptyList()
    private var currentQuery: String = ""
    private var favoritesOnly: Boolean = false

    fun load() {
        viewModelScope.launch {
            allItems = repository.getAll()
            applyFilters()
        }
    }

    fun setQuery(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun setFavoritesOnly(value: Boolean) {
        favoritesOnly = value
        applyFilters()
    }

    private fun applyFilters() {
        var result = allItems
        if (favoritesOnly) result = result.filter { it.isFavorite }
        if (currentQuery.isNotBlank()) {
            result = result.filter {
                it.displayTitle.contains(currentQuery, true) ||
                    it.rawContent.contains(currentQuery, true)
            }
        }
        _items.value = result
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
            load()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            load()
        }
    }

    fun deleteMultiple(ids: Set<String>) {
        viewModelScope.launch {
            repository.deleteMultiple(ids)
            load()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            load()
        }
    }

    fun exportToFile(destinationFile: File, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val gson = com.google.gson.Gson()
            try {
                destinationFile.writeText(gson.toJson(allItems))
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
