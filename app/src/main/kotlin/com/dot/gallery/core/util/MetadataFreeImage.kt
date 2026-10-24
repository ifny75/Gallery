package com.dot.gallery.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect

/** Copy only rendered pixels. No EXIF/XMP, gain map or source container is forwarded. */
fun Bitmap.metadataFreePixels(): Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { clean ->
    clean.eraseColor(Color.TRANSPARENT)
    val pixels = if (config == Bitmap.Config.HARDWARE) copy(Bitmap.Config.ARGB_8888, false) else this
    try {
        // Explicit pixel bounds avoid rescaling an image with a different density.
        Canvas(clean).drawBitmap(pixels, null, Rect(0, 0, width, height), null)
    } finally {
        if (pixels !== this) pixels.recycle()
    }
}
