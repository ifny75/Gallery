package com.dot.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dot.gallery.feature_node.presentation.edit.adjustments.Crop
import com.dot.gallery.feature_node.presentation.edit.adjustments.hasPendingCrop
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class CropTest {
    @Test fun onePixelCropIsAnEditAndSurvivesSaving() {
        val original = Bitmap.createBitmap(100, 80, Bitmap.Config.ARGB_8888)
        original.eraseColor(Color.BLUE)
        original.setPixel(1, 0, Color.RED)
        val selection = RectF(.01f, 0f, 1f, 1f)
        assertTrue(hasPendingCrop(selection, 100, 80))
        val cropped = Crop(selection).apply(original)
        assertEquals(99, cropped.width)
        assertEquals(80, cropped.height)
        assertEquals(Color.RED, cropped.getPixel(0, 0))
        val bytes = ByteArrayOutputStream().also { cropped.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertEquals(99, decoded.width)
        assertEquals(80, decoded.height)
        assertEquals(100, original.width)
        decoded.recycle(); cropped.recycle(); original.recycle()
    }

    @Test fun fullFrameAndSubpixelRoundingDoNotEnableSave() {
        assertFalse(hasPendingCrop(RectF(0f, 0f, 1f, 1f), 100, 80))
        assertFalse(hasPendingCrop(RectF(.0001f, 0f, .9999f, 1f), 100, 80))
    }

    @Test fun bottomRightEdgeAlwaysProducesValidPixels() {
        val original = Bitmap.createBitmap(100, 80, Bitmap.Config.ARGB_8888)
        val cropped = Crop(RectF(1f, 1f, 1f, 1f)).apply(original)
        assertEquals(1, cropped.width)
        assertEquals(1, cropped.height)
        cropped.recycle(); original.recycle()
    }
}
