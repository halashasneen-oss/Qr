package com.halashasneen.nova.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * طبقة عامة للتعامل مع تخزين البيانات على شكل ملفات JSON داخل التخزين الداخلي الخاص بالتطبيق.
 * هذه الطبقة هي نقطة الاستبدال الوحيدة إذا احتجنا لاحقًا الانتقال إلى قاعدة بيانات حقيقية (Room مثلاً)
 * دون التأثير على بقية طبقات الـ Repository.
 */
class JsonStorageHelper(private val context: Context) {

    private val gson = Gson()

    private fun fileFor(fileName: String): File = File(context.filesDir, fileName)

    suspend fun <T> readList(fileName: String, typeToken: TypeToken<List<T>>): List<T> =
        withContext(Dispatchers.IO) {
            val file = fileFor(fileName)
            if (!file.exists()) return@withContext emptyList()
            try {
                val json = file.readText()
                if (json.isBlank()) emptyList()
                else gson.fromJson(json, typeToken.type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun <T> writeList(fileName: String, data: List<T>) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(data)
            fileFor(fileName).writeText(json)
        } catch (e: Exception) {
            // Fail silently and keep the previous file intact to avoid data loss
        }
    }

    suspend fun <T> readObject(fileName: String, classOfT: Class<T>): T? =
        withContext(Dispatchers.IO) {
            val file = fileFor(fileName)
            if (!file.exists()) return@withContext null
            try {
                val json = file.readText()
                if (json.isBlank()) null else gson.fromJson(json, classOfT)
            } catch (e: Exception) {
                null
            }
        }

    suspend fun <T> writeObject(fileName: String, data: T) = withContext(Dispatchers.IO) {
        try {
            val json = gson.toJson(data)
            fileFor(fileName).writeText(json)
        } catch (e: Exception) {
            // Fail silently
        }
    }

    suspend fun exportToExternal(fileName: String, destinationFile: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val source = fileFor(fileName)
                if (!source.exists()) return@withContext false
                source.copyTo(destinationFile, overwrite = true)
                true
            } catch (e: Exception) {
                false
            }
        }

    suspend fun importFromExternal(fileName: String, sourceFile: File): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sourceFile.copyTo(fileFor(fileName), overwrite = true)
                true
            } catch (e: Exception) {
                false
            }
        }

    companion object {
        const val FILE_HISTORY = "history.json"
        const val FILE_VAULT = "vault.json"
        const val FILE_SETTINGS = "settings.json"
        const val FILE_STATS = "stats.json"
    }
}
