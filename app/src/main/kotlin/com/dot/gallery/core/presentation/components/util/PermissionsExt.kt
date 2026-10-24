package com.dot.gallery.core.presentation.components.util

import android.content.Context
import android.content.pm.PackageManager

fun Context.permissionGranted(list: List<String>): Boolean {
    var granted = true
    list.forEach {
        if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) granted = false
    }
    return granted
}
fun Context.hasMediaReadAccess(): Boolean = com.dot.gallery.core.MediaAccessPolicy.hasReadAccess(
    android.os.Build.VERSION.SDK_INT,
    (com.dot.gallery.core.Constants.PERMISSIONS + "android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
        .filter { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }.toSet()
)
