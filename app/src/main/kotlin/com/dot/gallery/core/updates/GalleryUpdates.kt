package com.dot.gallery.core.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dot.gallery.BuildConfig
import com.dot.gallery.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

private data class UpdateOffer(val name: String, val code: Long, val url: String)

/** Public release metadata only. No device identifiers or photo data leave the device. */
private object ReleaseChecker {
    private fun read(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "Gallery-update-check")
        return try {
            check(connection.responseCode == 200)
            connection.inputStream.bufferedReader().use { reader ->
                val text = CharArray(2_000_001).let { buffer ->
                    var count = 0
                    while (count < buffer.size) {
                        val n = reader.read(buffer, count, buffer.size - count)
                        if (n < 0) break
                        count += n
                    }
                    check(count <= 2_000_000)
                    String(buffer, 0, count)
                }
                text
            }
        } finally { connection.disconnect() }
    }
    suspend fun check(): UpdateOffer? = withContext(Dispatchers.IO) {
        val releases = JSONArray(read("https://api.github.com/repos/ifny75/Gallery/releases?per_page=5"))
        val offers = mutableListOf<UpdateOffer>()
        for (i in 0 until releases.length()) {
            val release = releases.getJSONObject(i)
            if (release.optBoolean("draft")) continue
            if (release.optBoolean("prerelease") && !BuildConfig.VERSION_NAME.contains("preview") && !BuildConfig.DEBUG) continue
            val assets = release.getJSONArray("assets")
            val urls = (0 until assets.length()).associate {
                val asset = assets.getJSONObject(it)
                asset.getString("name") to asset.getString("browser_download_url")
            }
            val manifest = urls["gallery-update.json"] ?: continue
            if (!ReleasePolicy.trustedAsset(manifest)) continue
            val update = ReleasePolicy.select(read(manifest), BuildConfig.APPLICATION_ID,
                BuildConfig.FLAVOR, BuildConfig.VERSION_CODE.toLong()) ?: continue
            val apkUrl = urls[update.apk] ?: continue
            if (ReleasePolicy.trustedAsset(apkUrl)) offers += UpdateOffer(update.versionName, update.versionCode, apkUrl)
        }
        offers.maxByOrNull { it.code }
    }
}

@Composable
fun GalleryUpdateNotice(manual: Boolean = false) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("gallery_updates", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(false) }
    var offer by remember {
        val code = prefs.getLong("offer_code", 0)
        val url = prefs.getString("offer_url", "").orEmpty()
        mutableStateOf(if (!manual && prefs.getString("offer_variant", "") == BuildConfig.FLAVOR && code > BuildConfig.VERSION_CODE && code != prefs.getLong("dismissed", -1) && ReleasePolicy.trustedAsset(url))
            UpdateOffer(prefs.getString("offer_name", "").orEmpty(), code, url) else null)
    }
    var confirm by remember { mutableStateOf(false) }
    fun checkUpdates(userInitiated: Boolean) {
        if (checking) return
        checking = true
        scope.launch {
            val result = runCatching { ReleaseChecker.check() }
            checking = false
            if (result.isSuccess) {
                prefs.edit().putLong("last_check", System.currentTimeMillis()).apply()
                val latest = result.getOrNull()
                prefs.edit().putLong("offer_code", latest?.code ?: 0)
                    .putString("offer_name", latest?.name).putString("offer_url", latest?.url)
                    .putString("offer_variant", BuildConfig.FLAVOR).apply()
                offer = latest?.takeIf { userInitiated || it.code != prefs.getLong("dismissed", -1) }
            }
            if (userInitiated && offer == null) Toast.makeText(context,
                if (result.isSuccess) R.string.gallery_update_none else R.string.gallery_update_error, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(Unit) {
        if (!manual && System.currentTimeMillis() - prefs.getLong("last_check", 0) > 12 * 60 * 60 * 1000L)
            checkUpdates(false)
    }
    if (manual) TextButton(onClick = { checkUpdates(true) }, enabled = !checking) {
        Text(stringResource(R.string.gallery_update_check))
        if (checking) CircularProgressIndicator(Modifier.padding(start = 12.dp).size(18.dp), strokeWidth = 2.dp)
    }
    offer?.let { update ->
        Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.secondaryContainer) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.gallery_update_available, update.name),
                    Modifier.weight(1f).clickable { confirm = true })
                TextButton(onClick = { confirm = true }) { Text(stringResource(R.string.gallery_update_download)) }
                TextButton(onClick = {
                    prefs.edit().putLong("dismissed", update.code).apply(); offer = null
                }) { Text(stringResource(R.string.gallery_update_later)) }
            }
        }
        if (confirm) AlertDialog(onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.gallery_update_available, update.name)) },
            text = { Text(stringResource(R.string.gallery_update_warning)) },
            confirmButton = { TextButton(onClick = {
                confirm = false
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.url))) }
                    .onFailure { Toast.makeText(context, R.string.gallery_update_error, Toast.LENGTH_LONG).show() }
            }) { Text(stringResource(R.string.gallery_update_download)) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(android.R.string.cancel)) } })
    }
}
