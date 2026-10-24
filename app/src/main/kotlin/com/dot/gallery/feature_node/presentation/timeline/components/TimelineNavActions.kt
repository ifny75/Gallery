/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */
package com.dot.gallery.feature_node.presentation.timeline.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.dot.gallery.R
import com.dot.gallery.core.LocalEventHandler
import com.dot.gallery.core.navigate
import com.dot.gallery.feature_node.presentation.util.Screen

@Composable
fun TimelineNavActions() {
    val eventHandler = LocalEventHandler.current
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.gallery_more_options))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.library)) }, onClick = {
            expanded = false; eventHandler.navigate(Screen.LibraryScreen())
        })
        DropdownMenuItem(text = { Text(stringResource(R.string.favorites)) }, onClick = {
            expanded = false; eventHandler.navigate(Screen.FavoriteScreen())
        })
        DropdownMenuItem(text = { Text(stringResource(R.string.settings_title)) }, onClick = {
            expanded = false; eventHandler.navigate(Screen.SettingsScreen())
        })
    }
}
