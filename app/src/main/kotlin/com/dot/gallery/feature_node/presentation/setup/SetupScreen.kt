package com.dot.gallery.feature_node.presentation.setup

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dot.gallery.R
import com.dot.gallery.core.Constants
import com.dot.gallery.core.presentation.components.SetupButton
import com.dot.gallery.core.presentation.components.SetupWizard
import com.dot.gallery.core.presentation.components.util.hasMediaReadAccess
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SetupScreen(onPermissionGranted: () -> Unit = {}) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasMediaReadAccess()) }
    var requested by remember { mutableStateOf(false) }
    val permissions = rememberMultiplePermissionsState(Constants.PERMISSIONS) {
        granted = context.hasMediaReadAccess()
        requested = true
    }
    LaunchedEffect(granted) { if (granted) onPermissionGranted() }
    SetupWizard(
        painter = painterResource(R.drawable.ic_launcher_foreground_monochrome),
        title = stringResource(R.string.app_name_gallery),
        subtitle = stringResource(R.string.gallery_setup_subtitle),
        contentPadding = 0.dp,
        bottomBar = {
            SetupButton(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier.weight(1f),
                applyHorizontalPadding = false, applyBottomPadding = false, applyInsets = false,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                text = stringResource(R.string.action_cancel)
            )
            SetupButton(
                onClick = { permissions.launchMultiplePermissionRequest() },
                modifier = Modifier.weight(1f),
                applyHorizontalPadding = false, applyBottomPadding = false, applyInsets = false,
                text = stringResource(R.string.get_started)
            )
        },
        content = {
        Text(
            text = stringResource(R.string.gallery_setup_media_only),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        if (requested && !granted) {
            Text(
                text = stringResource(R.string.gallery_setup_denied),
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        }
    )
}
