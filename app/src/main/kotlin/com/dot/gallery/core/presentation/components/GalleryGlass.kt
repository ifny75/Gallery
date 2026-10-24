package com.dot.gallery.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.dot.gallery.core.Settings.Misc.rememberAllowBlur
import com.dot.gallery.feature_node.presentation.util.LocalHazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/** One backdrop per panel, with a readable fallback when visual effects are disabled. */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun Modifier.galleryGlass(shape: Shape): Modifier {
    val blur by rememberAllowBlur()
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val backdrop = if (blur) Modifier.hazeEffect(
        state = LocalHazeState.current,
        style = HazeMaterials.thin(containerColor = surface)
    ) else Modifier.background(surface)
    return clip(shape).then(backdrop)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), shape)
}
