package com.halashasneen.nova.data.repository

import com.google.gson.reflect.TypeToken
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.data.storage.JsonStorageHelper

/**
 * مسؤول عن كل عمليات القراءة/الكتابة الخاصة بسجل المسح والإنشاء (History).
 * أي شاشة تحتاج بيانات السجل تتعامل مع هذا الـ Repository فقط ولا تلمس ملفات JSON مباشرة.
 */
class HistoryRepository(private val storage: JsonStorageHelper) {

    private val listType = object : TypeToken<List<QRHistoryItem>>() {}

    suspend fun getAll(): List<QRHistoryItem> =
        storage.readList(JsonStorageHelper.FILE_HISTORY, listType)
            .sortedByDescending { it.timestamp }

    suspend fun add(item: QRHistoryItem) {
        val current = getAll().toMutableList()
        current.add(0, item)
        storage.writeList(JsonStorageHelper.FILE_HISTORY, current)
    }

    suspend fun delete(id: String) {
        val current = getAll().filterNot { it.id == id }
        storage.writeList(JsonStorageHelper.FILE_HISTORY, current)
    }

    suspend fun deleteMultiple(ids: Set<String>) {
        val current = getAll().filterNot { it.id in ids }
        storage.writeList(JsonStorageHelper.FILE_HISTORY, current)
    }

    suspend fun clearAll() {
        storage.writeList(JsonStorageHelper.FILE_HISTORY, emptyList<QRHistoryItem>())
    }

    suspend fun toggleFavorite(id: String) {
        val current = getAll().map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        storage.writeList(JsonStorageHelper.FILE_HISTORY, current)
    }

    suspend fun getFavorites(): List<QRHistoryItem> = getAll().filter { it.isFavorite }
}
