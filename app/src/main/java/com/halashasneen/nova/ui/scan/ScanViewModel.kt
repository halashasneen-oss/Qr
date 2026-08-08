package com.halashasneen.nova.ui.scan

import androidx.lifecycle.ViewModel

/**
 * ViewModel بسيط لشاشة المسح. المنطق الأساسي (الكاميرا) يبقى داخل الـ Fragment لأنه
 * مرتبط بدورة حياة الواجهة (Preview/Lifecycle)، بينما هذا الصف مخصص لأي حالة إضافية مستقبلًا
 * (مثل تفعيل/تعطيل الفلاش) حتى تنجو من إعادة إنشاء الشاشة (rotation).
 */
class ScanViewModel : ViewModel() {
    var isFlashOn: Boolean = false
    var lastScannedValue: String? = null
}
