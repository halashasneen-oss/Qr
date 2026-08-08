package com.halashasneen.nova.util

import com.halashasneen.nova.data.model.QRType
import java.net.URL

/**
 * يحلل نص QR الخام ويحدد نوعه (رابط، هاتف، واي فاي...) ويستخرج عنوانًا مناسبًا للعرض.
 * هذا المنطق يُستخدم مرتين: بعد المسح (Scan) وأثناء إنشاء عنصر جديد للسجل.
 */
object QRContentParser {

    data class ParsedContent(
        val type: QRType,
        val displayTitle: String,
        val isSecure: Boolean? = null,
        val siteName: String? = null
    )

    fun parse(raw: String): ParsedContent {
        val trimmed = raw.trim()
        return when {
            trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true) ->
                parseUrl(trimmed)

            trimmed.contains("wa.me") || trimmed.contains("whatsapp.com") ->
                ParsedContent(QRType.WHATSAPP, "واتساب", siteName = "WhatsApp")

            trimmed.contains("t.me") || trimmed.contains("telegram.me") ->
                ParsedContent(QRType.TELEGRAM, "تيليجرام", siteName = "Telegram")

            trimmed.contains("instagram.com") ->
                ParsedContent(QRType.INSTAGRAM, "انستغرام", siteName = "Instagram")

            trimmed.contains("facebook.com") || trimmed.contains("fb.com") ->
                ParsedContent(QRType.FACEBOOK, "فيسبوك", siteName = "Facebook")

            trimmed.contains("youtube.com") || trimmed.contains("youtu.be") ->
                ParsedContent(QRType.YOUTUBE, "يوتيوب", siteName = "YouTube")

            trimmed.startsWith("tel:", true) ->
                ParsedContent(QRType.PHONE, trimmed.removePrefix("tel:"))

            trimmed.startsWith("mailto:", true) ->
                ParsedContent(QRType.EMAIL, trimmed.removePrefix("mailto:").substringBefore("?"))

            trimmed.startsWith("smsto:", true) || trimmed.startsWith("sms:", true) ->
                ParsedContent(QRType.SMS, trimmed.substringAfter(":").substringBefore(":"))

            trimmed.startsWith("WIFI:", true) ->
                ParsedContent(QRType.WIFI, extractWifiSsid(trimmed))

            trimmed.startsWith("BEGIN:VCARD", true) ->
                ParsedContent(QRType.CONTACT, extractVCardName(trimmed))

            trimmed.startsWith("geo:", true) ->
                ParsedContent(QRType.LOCATION, "موقع جغرافي")

            else -> ParsedContent(QRType.TEXT, trimmed.take(40))
        }
    }

    private fun parseUrl(url: String): ParsedContent {
        val isSecure = url.startsWith("https://", true)
        val host = try {
            URL(url).host
        } catch (e: Exception) {
            null
        }
        return ParsedContent(
            type = QRType.WEBSITE,
            displayTitle = host ?: url.take(40),
            isSecure = isSecure,
            siteName = host
        )
    }

    private fun extractWifiSsid(raw: String): String {
        val regex = Regex("S:([^;]*);")
        return regex.find(raw)?.groupValues?.get(1) ?: "شبكة Wi-Fi"
    }

    private fun extractVCardName(raw: String): String {
        val regex = Regex("FN:([^\\n\\r]*)")
        return regex.find(raw)?.groupValues?.get(1)?.trim() ?: "جهة اتصال"
    }
}
