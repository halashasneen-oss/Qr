package com.halashasneen.nova.ui.scan

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * محلل إطارات الكاميرا لاستخراج أكواد QR باستخدام ML Kit (يعمل بالكامل على الجهاز بدون إنترنت).
 * تم اختيار ML Kit بدل ZXing لعملية المسح الحية لأن أداءه على تدفق الكاميرا أسرع وأكثر استقرارًا،
 * بينما تبقى ZXing مستخدمة فقط لتوليد صور QR في شاشة الإنشاء.
 */
class QRAnalyzer(
    private val onQrDetected: (String) -> Unit
) : androidx.camera.core.ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient()
    private val isProcessing = AtomicBoolean(false)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing.get()) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        isProcessing.set(true)
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull { it.valueType != Barcode.TYPE_UNKNOWN }
                    ?.rawValue ?: barcodes.firstOrNull()?.rawValue
                if (!value.isNullOrBlank()) {
                    onQrDetected(value)
                }
            }
            .addOnCompleteListener {
                isProcessing.set(false)
                imageProxy.close()
            }
    }
}
