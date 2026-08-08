package com.halashasneen.nova.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.AppStats
import com.halashasneen.nova.data.repository.HistoryRepository
import com.halashasneen.nova.data.repository.StatsRepository
import com.halashasneen.nova.data.repository.VaultRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val historyRepository: HistoryRepository,
    private val vaultRepository: VaultRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _stats = MutableLiveData<AppStats>()
    val stats: LiveData<AppStats> = _stats

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = statsRepository.computeStats()
        }
    }
}
