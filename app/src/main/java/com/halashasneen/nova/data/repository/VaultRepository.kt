package com.halashasneen.nova.data.repository

import com.google.gson.reflect.TypeToken
import com.halashasneen.nova.data.model.VaultItem
import com.halashasneen.nova.data.storage.JsonStorageHelper

/** مسؤول عن العناصر المحفوظة داخل الـ Vault المحمي */
class VaultRepository(private val storage: JsonStorageHelper) {

    private val listType = object : TypeToken<List<VaultItem>>() {}

    suspend fun getAll(): List<VaultItem> =
        storage.readList(JsonStorageHelper.FILE_VAULT, listType)
            .sortedByDescending { it.timestamp }

    suspend fun add(item: VaultItem) {
        val current = getAll().toMutableList()
        current.add(0, item)
        storage.writeList(JsonStorageHelper.FILE_VAULT, current)
    }

    suspend fun delete(id: String) {
        val current = getAll().filterNot { it.id == id }
        storage.writeList(JsonStorageHelper.FILE_VAULT, current)
    }

    suspend fun count(): Int = getAll().size
}
