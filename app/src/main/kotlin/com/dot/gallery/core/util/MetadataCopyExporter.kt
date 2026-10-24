package com.dot.gallery.core.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dot.gallery.core.util.ext.saveImage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class MetadataCopyExporter @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun export(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        val target = Glide.with(context).asBitmap().disallowHardwareConfig()
            .load(uri).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).submit()
        try {
            val bitmap = runInterruptible { target.get() }
            exportBitmap(bitmap)
        } finally {
            Glide.with(context).clear(target)
        }
    }

    suspend fun exportBitmap(bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val clean = bitmap.metadataFreePixels()
        try {
            context.contentResolver.saveImage(
                bitmap = clean, format = Bitmap.CompressFormat.PNG, mimeType = "image/png",
                relativePath = "Pictures/Private",
                displayName = "Photo_${UUID.randomUUID()}.png"
            )
        } finally {
            clean.recycle()
        }
    }
}
