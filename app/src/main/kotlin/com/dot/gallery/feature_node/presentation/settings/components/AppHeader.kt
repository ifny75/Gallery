/* SPDX-License-Identifier: Apache-2.0 */
package com.dot.gallery.feature_node.presentation.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dot.gallery.BuildConfig
import com.dot.gallery.R
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigate
import com.dot.gallery.feature_node.presentation.util.Screen

@Composable
fun SettingsAppHeader() {
    val events = LocalEventHandler.current
    Card(
        onClick = { events.navigate(Screen.AuthorsScreen()) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(64.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.gallery_authors), style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary)
                Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
