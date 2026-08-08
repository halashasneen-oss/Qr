package com.halashasneen.nova.data.model

/** خيارات تخصيص شكل QR المولّد */
data class QRCustomization(
    val foregroundColor: Int = 0xFF000000.toInt(),
    val backgroundColor: Int = 0xFFFFFFFF.toInt(),
    val dotShape: DotShape = DotShape.SQUARE,
    val cornerShape: CornerShape = CornerShape.SQUARE,
    val logoPath: String? = null,
    val hasFrame: Boolean = false,
    val frameText: String? = null
)

enum class DotShape { SQUARE, ROUNDED, CIRCLE }
enum class CornerShape { SQUARE, ROUNDED, DOTS }
