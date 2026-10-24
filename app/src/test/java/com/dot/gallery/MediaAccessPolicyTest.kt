package com.dot.gallery

import com.dot.gallery.core.MediaAccessPolicy
import org.junit.Assert.*
import org.junit.Test

class MediaAccessPolicyTest {
    @Test fun startupRequestsOnlyVisualMedia() {
        for (sdk in 29..37) {
            val permissions = MediaAccessPolicy.requestedPermissions(sdk)
            assertTrue(permissions.isNotEmpty())
            assertTrue(permissions.all { it.substringAfterLast('.') in setOf(
                "READ_EXTERNAL_STORAGE", "READ_MEDIA_IMAGES", "READ_MEDIA_VIDEO", "READ_MEDIA_VISUAL_USER_SELECTED"
            ) })
        }
    }

    @Test fun selectedPhotosAreEnoughOnAndroid14AndLater() {
        for (sdk in 34..37) {
            assertTrue(MediaAccessPolicy.hasReadAccess(sdk, setOf("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")))
        }
    }

    @Test fun imagesOrVideosAloneAreEnough() {
        for (sdk in 33..37) {
            assertTrue(MediaAccessPolicy.hasReadAccess(sdk, setOf("android.permission.READ_MEDIA_IMAGES")))
            assertTrue(MediaAccessPolicy.hasReadAccess(sdk, setOf("android.permission.READ_MEDIA_VIDEO")))
        }
    }

    @Test fun unrelatedAccessDoesNotBypassSetup() {
        for (sdk in 29..37) {
            assertFalse(MediaAccessPolicy.hasReadAccess(sdk, emptySet()))
            assertFalse(MediaAccessPolicy.hasReadAccess(sdk, setOf(
                "android.permission.POST_NOTIFICATIONS", "android.permission.ACCESS_MEDIA_LOCATION",
                "android.permission.MANAGE_EXTERNAL_STORAGE"
            )))
        }
    }

    @Test fun legacyStorageIsSupported() {
        for (sdk in 29..32) {
            assertTrue(MediaAccessPolicy.hasReadAccess(sdk, setOf("android.permission.READ_EXTERNAL_STORAGE")))
        }
        assertFalse(MediaAccessPolicy.hasReadAccess(33, setOf("android.permission.READ_EXTERNAL_STORAGE")))
    }
}
