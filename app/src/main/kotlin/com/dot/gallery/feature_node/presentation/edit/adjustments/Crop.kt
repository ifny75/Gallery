package com.dot.gallery.feature_node.presentation.edit.adjustments

import android.graphics.Bitmap
import android.graphics.RectF
import com.dot.gallery.feature_node.domain.model.editor.Adjustment
import kotlin.math.roundToInt

/** Ignore subpixel layout rounding, but treat even a one-pixel crop as an edit. */
fun hasPendingCrop(rect: RectF, width: Int, height: Int): Boolean =
    !rect.isEmpty && ((rect.left * width).roundToInt() > 0 || (rect.top * height).roundToInt() > 0 ||
        (rect.right * width).roundToInt() < width || (rect.bottom * height).roundToInt() < height)

/**
 * Crop adjustment that stores a normalized crop rect (0-1 range).
 * [apply] crops [bitmap] using the normalized rect, producing a full-resolution result
 * regardless of the preview bitmap size used during editing.
 */
data class Crop(val normalizedRect: RectF): Adjustment {

    override fun apply(bitmap: Bitmap): Bitmap {
        val x = (normalizedRect.left * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1)
        val y = (normalizedRect.top * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1)
        val right = (normalizedRect.right * bitmap.width).roundToInt().coerceIn(x + 1, bitmap.width)
        val bottom = (normalizedRect.bottom * bitmap.height).roundToInt().coerceIn(y + 1, bitmap.height)
        val w = right - x
        val h = bottom - y
        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }

}
