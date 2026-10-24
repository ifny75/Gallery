package com.dot.gallery.core

/** Startup never depends on location, notifications or special storage access. */
object MediaAccessPolicy {
    private const val IMAGES = "android.permission.READ_MEDIA_IMAGES"
    private const val VIDEOS = "android.permission.READ_MEDIA_VIDEO"
    private const val SELECTED = "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
    private const val STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

    fun requestedPermissions(sdk: Int): List<String> = when {
        sdk >= 34 -> listOf(IMAGES, VIDEOS, SELECTED)
        sdk >= 33 -> listOf(IMAGES, VIDEOS)
        else -> listOf(STORAGE)
    }

    fun hasReadAccess(sdk: Int, granted: Set<String>): Boolean = when {
        sdk >= 34 -> IMAGES in granted || VIDEOS in granted || SELECTED in granted
        sdk >= 33 -> IMAGES in granted || VIDEOS in granted
        else -> STORAGE in granted
    }
}
