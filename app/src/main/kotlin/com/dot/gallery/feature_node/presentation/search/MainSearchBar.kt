/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */
package com.dot.gallery.feature_node.presentation.search

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dot.gallery.R
import com.dot.gallery.core.presentation.components.GalleryPageHeader

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainSearchBar(
    isScrolling: MutableState<Boolean>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    title: String = stringResource(R.string.gallery_all_pictures),
    icon: ImageVector = Icons.Outlined.Photo,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    menuItems: @Composable (RowScope.() -> Unit)? = null,
) {
    GalleryPageHeader(title = title, icon = icon, scrollBehavior = scrollBehavior) { menuItems?.invoke(this) }
}
