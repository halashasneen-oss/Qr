package com.halashasneen.nova.ui.create

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.halashasneen.nova.data.model.CornerShape
import com.halashasneen.nova.data.model.DotShape
import com.halashasneen.nova.data.model.QRCustomization

/**
 * يولّد صورة QR اعتمادًا على مكتبة ZXing، مع رسم يدوي فوق المصفوفة الناتجة لدعم
 * تخصيص شكل النقاط/الزوايا واللون والخلفية (ZXing نفسها لا توفر تخصيصًا بصريًا، لذلك
 * نستخدمها فقط لحساب المصفوفة الثنائية الصحيحة للكود، والرسم يتم يدويًا فوق Canvas).
 */
object QRCodeGenerator {

    fun generate(
        content: String,
        sizePx: Int,
        customization: QRCustomization = QRCustomization()
    ): Bitmap {
        val writer = QRCodeWriter()
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )
        val matrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(customization.backgroundColor)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = customization.foregroundColor
            style = Paint.Style.FILL
        }

        val moduleCount = matrix.width
        val moduleSize = sizePx.toFloat() / moduleCount

        for (y in 0 until moduleCount) {
            for (x in 0 until moduleCount) {
                if (!matrix.get(x, y)) continue
                val isCornerFinder = isFinderPatternModule(x, y, moduleCount)
                val left = x * moduleSize
                val top = y * moduleSize
                val rect = RectF(left, top, left + moduleSize, top + moduleSize)

                when {
                    isCornerFinder && customization.cornerShape == CornerShape.ROUNDED ->
                        canvas.drawRoundRect(rect, moduleSize * 0.3f, moduleSize * 0.3f, paint)
                    isCornerFinder && customization.cornerShape == CornerShape.DOTS ->
                        canvas.drawCircle(rect.centerX(), rect.centerY(), moduleSize / 2f, paint)
                    !isCornerFinder && customization.dotShape == DotShape.CIRCLE ->
                        canvas.drawCircle(rect.centerX(), rect.centerY(), moduleSize / 2.1f, paint)
                    !isCornerFinder && customization.dotShape == DotShape.ROUNDED ->
                        canvas.drawRoundRect(rect, moduleSize * 0.25f, moduleSize * 0.25f, paint)
                    else -> canvas.drawRect(rect, paint)
                }
            }
        }

        return if (customization.hasFrame) addFrame(bitmap, customization) else bitmap
    }

    /** يحدد إذا كانت الوحدة جزءًا من مربعات التوجيه الثلاثة الكبيرة في زوايا الكود */
    private fun isFinderPatternModule(x: Int, y: Int, moduleCount: Int): Boolean {
        val finderSize = 7
        val inTopLeft = x < finderSize && y < finderSize
        val inTopRight = x >= moduleCount - finderSize && y < finderSize
        val inBottomLeft = x < finderSize && y >= moduleCount - finderSize
        return inTopLeft || inTopRight || inBottomLeft
    }

    private fun addFrame(source: Bitmap, customization: QRCustomization): Bitmap {
        val framePadding = (source.width * 0.18f).toInt()
        val textHeight = if (customization.frameText.isNullOrBlank()) 0 else (source.width * 0.12f).toInt()
        val newWidth = source.width + framePadding * 2
        val newHeight = source.height + framePadding * 2 + textHeight

        val result = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(customization.backgroundColor)

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = customization.foregroundColor
            style = Paint.Style.STROKE
            strokeWidth = framePadding * 0.12f
        }
        val frameRect = RectF(
            framePadding * 0.4f, framePadding * 0.4f,
            newWidth - framePadding * 0.4f, newHeight - framePadding * 0.4f
        )
        canvas.drawRoundRect(frameRect, 24f, 24f, framePaint)
        canvas.drawBitmap(source, framePadding.toFloat(), framePadding.toFloat(), null)

        if (!customization.frameText.isNullOrBlank()) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = customization.foregroundColor
                textSize = textHeight * 0.5f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                customization.frameText,
                newWidth / 2f,
                (newHeight - textHeight / 2.2f),
                textPaint
            )
        }

        return result
    }
}
