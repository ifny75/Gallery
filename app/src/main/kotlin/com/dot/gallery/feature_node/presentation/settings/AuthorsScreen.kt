package com.dot.gallery.feature_node.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dot.gallery.R
import com.dot.gallery.feature_node.presentation.settings.components.BaseSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorsScreen() {
    val uriHandler = LocalUriHandler.current
    BaseSettingsScreen(
        title = stringResource(R.string.gallery_authors),
        settingsList = remember { mutableStateListOf() },
        topContent = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.gallery_authors_intro), style = MaterialTheme.typography.titleLarge)
                listOf(
                    R.string.gallery_credit_refra to "https://github.com/IacobIonut01/Gallery",
                    R.string.gallery_credit_graphene to "https://github.com/GrapheneOS/Gallery",
                    R.string.gallery_credit_contributors to "https://github.com/ifny75/Gallery/graphs/contributors"
                ).forEach { (text, url) ->
                    OutlinedCard(onClick = { uriHandler.openUri(url) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(text), modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    )
}
