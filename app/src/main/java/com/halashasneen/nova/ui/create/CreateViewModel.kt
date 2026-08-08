package com.halashasneen.nova.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.data.repository.HistoryRepository
import kotlinx.coroutines.launch

class CreateViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    /** يبني نص محتوى QR الصحيح حسب المعيار القياسي لكل نوع (vCard, WIFI, mailto...) */
    fun buildContent(type: QRType, fields: Map<String, String>): String {
        return when (type) {
            QRType.WEBSITE -> normalizeUrl(fields["url"].orEmpty())
            QRType.TEXT -> fields["text"].orEmpty()
            QRType.PHONE -> "tel:${fields["phone"].orEmpty()}"
            QRType.EMAIL -> {
                val subject = fields["subject"].orEmpty()
                val body = fields["body"].orEmpty()
                "mailto:${fields["email"].orEmpty()}?subject=${enc(subject)}&body=${enc(body)}"
            }
            QRType.SMS -> "smsto:${fields["phone"].orEmpty()}:${fields["message"].orEmpty()}"
            QRType.WIFI -> {
                val security = fields["security"].orEmpty().ifBlank { "WPA" }
                "WIFI:T:$security;S:${fields["ssid"].orEmpty()};P:${fields["password"].orEmpty()};;"
            }
            QRType.CONTACT -> buildString {
                append("BEGIN:VCARD\nVERSION:3.0\n")
                append("FN:${fields["name"].orEmpty()}\n")
                if (!fields["phone"].isNullOrBlank()) append("TEL:${fields["phone"]}\n")
                if (!fields["email"].isNullOrBlank()) append("EMAIL:${fields["email"]}\n")
                if (!fields["company"].isNullOrBlank()) append("ORG:${fields["company"]}\n")
                append("END:VCARD")
            }
            QRType.LOCATION -> "geo:${fields["lat"].orEmpty()},${fields["lng"].orEmpty()}"
            QRType.WHATSAPP -> {
                val number = fields["phone"].orEmpty().filter { it.isDigit() }
                val text = fields["message"].orEmpty()
                "https://wa.me/$number?text=${enc(text)}"
            }
            QRType.TELEGRAM -> "https://t.me/${fields["username"].orEmpty().removePrefix("@")}"
            QRType.INSTAGRAM -> "https://instagram.com/${fields["username"].orEmpty().removePrefix("@")}"
            QRType.FACEBOOK -> normalizeUrl(fields["url"].orEmpty())
            QRType.YOUTUBE -> normalizeUrl(fields["url"].orEmpty())
            QRType.UNKNOWN -> fields["text"].orEmpty()
        }
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return trimmed
        return if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    private fun enc(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    fun saveGeneratedToHistory(rawContent: String, type: QRType, displayTitle: String) {
        viewModelScope.launch {
            historyRepository.add(
                QRHistoryItem(
                    rawContent = rawContent,
                    type = type,
                    displayTitle = displayTitle,
                    isGenerated = true
                )
            )
        }
    }
}
