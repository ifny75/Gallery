package com.dot.gallery.core.updates

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI

@Serializable
data class GalleryUpdate(val applicationId: String, val variant: String, val versionCode: Long,
    val versionName: String, val apk: String)
@Serializable
data class GalleryUpdateManifest(val schema: Int, val updates: List<GalleryUpdate>)

object ReleasePolicy {
    private val json = Json { ignoreUnknownKeys = true }
    fun select(manifest: String, packageName: String, variant: String, installed: Long): GalleryUpdate? {
        val data = runCatching { json.decodeFromString<GalleryUpdateManifest>(manifest) }.getOrNull() ?: return null
        if (data.schema != 1) return null
        return data.updates.filter { it.applicationId == packageName && it.variant == variant &&
            it.versionCode > installed && it.versionName.isNotBlank() && it.apk.matches(Regex("[A-Za-z0-9._-]+\\.apk")) }
            .maxByOrNull { it.versionCode }
    }
    fun trustedAsset(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "github.com" && uri.userInfo == null && uri.port == -1 &&
            uri.path.startsWith("/ifny75/Gallery/releases/download/") && uri.query == null && uri.fragment == null &&
            !uri.path.contains("/../")
    }.getOrDefault(false)
}
