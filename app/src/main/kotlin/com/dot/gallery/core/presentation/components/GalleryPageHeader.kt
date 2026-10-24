/* SPDX-License-Identifier: Apache-2.0 */
package com.dot.gallery.core.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberGalleryScrollBehavior(): TopAppBarScrollBehavior {
    val delegate = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    return remember(delegate) {
        object : TopAppBarScrollBehavior by delegate {
            override val nestedScrollConnection = object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    delegate.state.heightOffset += available.y
                    // Observe the gesture; never spend the photo grid's scroll on the title.
                    return Offset.Zero
                }
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    delegate.state.contentOffset += consumed.y
                    return Offset.Zero
                }
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    delegate.nestedScrollConnection.onPostFling(consumed, available)
                    return Velocity.Zero
                }
            }
        }
    }
}

/** One title moves and scales into a persistent toolbar; no duplicated/teleporting text. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryPageHeader(
    title: String,
    icon: ImageVector,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val expandedHeight = if (landscape) 104.dp else 176.dp
    val density = LocalDensity.current
    val expandedPx = with(density) { expandedHeight.toPx() }
    val compactPx = with(density) { 64.dp.toPx() }
    SideEffect {
        scrollBehavior?.state?.let { state ->
            val limit = compactPx - expandedPx
            if (state.heightOffsetLimit != limit) {
                val fraction = state.collapsedFraction.coerceIn(0f, 1f)
                state.heightOffsetLimit = limit
                state.heightOffset = limit * fraction
            }
        }
    }
    Layout(
        modifier = Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding().clipToBounds().testTag("gallery_header"),
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp))
                Text(title, fontSize = 34.sp, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground)
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, content = actions)
        }
    ) { measurables, constraints ->
        val state = scrollBehavior?.state
        val fraction = (state?.collapsedFraction ?: 0f).coerceIn(0f, 1f)
        val height = (expandedPx + (state?.heightOffset ?: 0f)).coerceIn(compactPx, expandedPx).roundToInt()
        val width = constraints.maxWidth
        val edge = 16.dp.roundToPx()
        val controls = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0, maxHeight = compactPx.roundToInt()))
        val heading = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0,
            maxWidth = (width - edge * 2 - controls.width).coerceAtLeast(0)))
        val scale = 1f + (22f / 34f - 1f) * fraction
        val startX = (width - heading.width) / 2f
        val x = startX + (edge - startX) * fraction
        val startY = (expandedPx - heading.height) / 2f
        val endY = (compactPx - heading.height * scale) / 2f
        val y = startY + (endY - startY) * fraction
        layout(width, height) {
            heading.placeRelativeWithLayer(x.roundToInt(), y.roundToInt()) {
                scaleX = scale; scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
            controls.placeRelative(width - controls.width - 8.dp.roundToPx(), ((compactPx - controls.height) / 2f).roundToInt())
        }
    }
}
