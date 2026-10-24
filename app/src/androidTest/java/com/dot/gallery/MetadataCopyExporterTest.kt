package com.dot.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dot.gallery.core.util.MetadataCopyExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MetadataCopyExporterTest {
    @Test fun selectedPhotosExportSequentiallyWithoutMetadataOrSourceChanges() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val exporter = MetadataCopyExporter(context)
        val sources = mutableListOf<File>()
        val outputs = mutableListOf<Uri>()
        try {
            repeat(2) { index ->
                val source = File.createTempFile("batch-metadata-fixture-", ".jpg", context.cacheDir)
                sources += source
                val bitmap = Bitmap.createBitmap(32, 16, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(if (index == 0) Color.RED else Color.BLUE)
                source.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
                bitmap.recycle()
                ExifInterface(source).apply {
                    setAttribute(ExifInterface.TAG_MAKE, "Private camera")
                    setAttribute(ExifInterface.TAG_USER_COMMENT, "Private comment")
                    setLatLong(55.75, 37.61)
                    setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                    saveAttributes()
                }
                val original = source.readBytes()
                val output = exporter.export(Uri.fromFile(source))
                assertNotNull(output)
                outputs += output!!
                val bytes = context.contentResolver.openInputStream(output)!!.use { it.readBytes() }
                val exif = ExifInterface(bytes.inputStream())
                assertNull(exif.latLong)
                assertNull(exif.getAttribute(ExifInterface.TAG_MAKE))
                assertNull(exif.getAttribute(ExifInterface.TAG_USER_COMMENT))
                val result = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                assertEquals(16, result.width)
                assertEquals(32, result.height)
                result.recycle()
                assertArrayEquals(original, source.readBytes())
            }
            assertEquals(2, outputs.distinct().size)
            var failed = false
            try { exporter.export(Uri.fromFile(File(context.cacheDir, "does-not-exist-gallery-fixture.jpg"))) }
            catch (_: Exception) { failed = true }
            assertTrue("Unreadable sources must report failure", failed)
        } finally {
            outputs.forEach { context.contentResolver.delete(it, null, null) }
            sources.forEach { it.delete() }
        }
    }
}
