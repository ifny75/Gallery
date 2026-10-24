package com.dot.gallery
import com.dot.gallery.core.updates.ReleasePolicy
import org.junit.Assert.*
import org.junit.Test

class ReleasePolicyTest {
    private fun manifest(pkg: String = "com.dot.gallery.debug", variant: String = "arm64-v8aNoML", code: Int = 421034, apk: String = "Gallery.apk", schema: Int = 1) =
        """{"schema":$schema,"updates":[{"applicationId":"$pkg","variant":"$variant","versionCode":$code,"versionName":"4.2.3","apk":"$apk"}]}"""
    private fun select(text: String) = ReleasePolicy.select(text, "com.dot.gallery.debug", "arm64-v8aNoML", 421024)
    @Test fun newerMatchingBuildIsOffered() { assertEquals(421034L, select(manifest())?.versionCode) }
    @Test fun downgradeAndSameVersionAreRejected() {
        assertNull(select(manifest(code=421024))); assertNull(select(manifest(code=421014)))
    }
    @Test fun incompatiblePackagesAndVariantsAreRejected() {
        assertNull(select(manifest(pkg="com.dot.gallery"))); assertNull(select(manifest(variant="x86NoML")))
    }
    @Test fun malformedOrUnknownManifestsAreRejected() {
        assertNull(select("garbage")); assertNull(select(manifest(schema=2)))
        assertNull(select(manifest(apk="../Gallery.apk")))
    }
    @Test fun onlyRepositoryReleaseAssetsAreTrusted() {
        assertTrue(ReleasePolicy.trustedAsset("https://github.com/ifny75/Gallery/releases/download/v4.2.2/Gallery.apk"))
        listOf("http://github.com/ifny75/Gallery/releases/download/a/x.apk",
            "https://github.com.evil.test/ifny75/Gallery/releases/download/a/x.apk",
            "https://github.com/other/Gallery/releases/download/a/x.apk",
            "https://github.com/ifny75/Gallery/releases/download/../evil.apk").forEach { assertFalse(ReleasePolicy.trustedAsset(it)) }
    }
}
