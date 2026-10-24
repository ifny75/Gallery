package com.dot.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bumptech.glide.Glide
import com.dot.gallery.core.util.metadataFreePixels
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(AndroidJUnit4::class)
class MetadataFreeImageTest {
    @Test fun hardwareBitmapCanBeExported() {
        val software = Bitmap.createBitmap(8, 4, Bitmap.Config.ARGB_8888)
        software.eraseColor(Color.MAGENTA)
        val hardware = software.copy(Bitmap.Config.HARDWARE, false)
        val clean = hardware.metadataFreePixels()
        assertTrue(software.sameAs(clean))
        assertFalse(hardware.isRecycled)
        clean.recycle()
        hardware.recycle()
        software.recycle()
    }

    @Test fun pngPreservesPixelsDimensionsAndTransparency() {
        val source = Bitmap.createBitmap(4, 3, Bitmap.Config.ARGB_8888)
        source.density = 72
        source.eraseColor(Color.BLUE)
        source.setPixel(1, 1, Color.TRANSPARENT)
        source.setPixel(2, 2, Color.RED)
        val clean = source.metadataFreePixels()
        val bytes = ByteArrayOutputStream().also { assertTrue(clean.compress(Bitmap.CompressFormat.PNG, 100, it)) }.toByteArray()
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        assertTrue(source.sameAs(decoded))
        assertFalse(clean === source)
        clean.recycle()
        assertFalse(source.isRecycled)
        source.recycle()
        decoded.recycle()
    }

    @Test fun cleanExportRemovesGpsCameraDateCommentsAndXmpWithoutChangingSource() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File.createTempFile("metadata-fixture-", ".jpg", context.cacheDir)
        var decoded: Bitmap? = null
        var clean: Bitmap? = null
        try {
            val source = Bitmap.createBitmap(16, 8, Bitmap.Config.ARGB_8888)
            source.eraseColor(Color.GREEN)
            file.outputStream().use { source.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            source.recycle()
            val tags = mapOf(
                ExifInterface.TAG_MAKE to "Private camera",
                ExifInterface.TAG_MODEL to "Private model",
                ExifInterface.TAG_DATETIME_ORIGINAL to "2020:01:02 03:04:05",
                ExifInterface.TAG_USER_COMMENT to "Private comment",
                ExifInterface.TAG_XMP to "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">Private XMP</x:xmpmeta>"
            )
            ExifInterface(file).apply {
                tags.forEach { (tag, value) -> setAttribute(tag, value) }
                setLatLong(55.75, 37.61)
                setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                saveAttributes()
            }
            val originalBytes = file.readBytes()
            // Use the same orientation-aware decoder as the editor.
            val request = Glide.with(context).asBitmap().load(file).submit()
            decoded = request.get()
            assertEquals(8, decoded.width)
            assertEquals(16, decoded.height)
            clean = decoded.metadataFreePixels()
            val bytes = ByteArrayOutputStream().also { clean.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()
            val exif = ExifInterface(ByteArrayInputStream(bytes))
            tags.keys.forEach { assertNull("Unexpected tag: $it", exif.getAttribute(it)) }
            assertNull(exif.latLong)
            assertTrue(originalBytes.contentEquals(file.readBytes()))
            val exported = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            assertTrue(clean.sameAs(exported))
            exported.recycle()
            Glide.with(context).clear(request)
        } finally {
            clean?.recycle()
            file.delete()
        }
    }
}
