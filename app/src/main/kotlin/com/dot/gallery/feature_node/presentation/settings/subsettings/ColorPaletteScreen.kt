package com.dot.gallery.feature_node.presentation.settings.subsettings

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dot.gallery.R
import com.dot.gallery.core.Settings
import com.dot.gallery.core.presentation.components.*
import com.dot.gallery.feature_node.presentation.util.LocalHazeState
import com.dot.gallery.feature_node.presentation.util.Screen
import com.dot.gallery.ui.theme.colorSchemeFromSeed
import com.dot.gallery.ui.theme.neutralColorScheme
import com.dot.gallery.ui.theme.isDarkTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteScreen() {
    var seed by Settings.Misc.rememberThemeColorSeed()
    var forceTheme by Settings.Misc.rememberForceTheme()
    var dark by Settings.Misc.rememberIsDarkMode()
    var amoled by Settings.Misc.rememberIsAmoledMode()
    var blur by Settings.Misc.rememberAllowBlur()
    var contrast by Settings.Misc.rememberAutoContrast()
    var transitions by Settings.Misc.rememberSharedElements()
    val isDark = isDarkTheme()
    val behavior = rememberGalleryScrollBehavior()
    val current = MaterialTheme.colorScheme
    val previewColors = remember(seed, isDark, amoled, current) {
        val chosen = when (seed) {
            Settings.Misc.THEME_SEED_SYSTEM -> current
            Settings.Misc.THEME_SEED_NEUTRAL -> neutralColorScheme(isDark, amoled)
            else -> colorSchemeFromSeed(seed.toLongOrNull(16)?.toInt() ?: current.primary.hashCode(), isDark, amoled)
        }
        if (isDark) chosen.copy(background = Color(0xFF080808), surface = Color(0xFF080808),
            surfaceContainer = Color(0xFF1B1B1B), surfaceContainerLow = Color(0xFF101010),
            onSurface = Color(0xFFFAFAFA), onBackground = Color(0xFFFAFAFA), onSurfaceVariant = Color(0xFFAAAAAA))
        else chosen
    }
    Scaffold(
        modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.settings_appearance)) },
                navigationIcon = { NavigationBackButton() }, scrollBehavior = behavior,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = current.background,
                    scrolledContainerColor = current.background))
        }
    ) { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp,
                    bottom = padding.calculateBottomPadding() + 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MaterialTheme(colorScheme = previewColors) {
                        GalleryAppearancePreview(Modifier.padding(horizontal = 20.dp))
                    }
                }
                item {
                    Text(stringResource(R.string.gallery_appearance_colors),
                        modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.titleLarge)
                }
                item {
                    Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(selected = seed == Settings.Misc.THEME_SEED_SYSTEM,
                            onClick = { seed = Settings.Misc.THEME_SEED_SYSTEM },
                            label = { Text(stringResource(R.string.gallery_appearance_system)) })
                        FilterChip(selected = seed == Settings.Misc.THEME_SEED_NEUTRAL,
                            onClick = { seed = Settings.Misc.THEME_SEED_NEUTRAL },
                            label = { Text(stringResource(R.string.gallery_appearance_neutral)) })
                    }
                }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(presetPalettes, key = { it.hexKey }) { palette ->
                            val color = Color(palette.seedArgb)
                            Box(Modifier.size(48.dp).clip(CircleShape).background(color)
                                .border(if (seed == palette.hexKey) 3.dp else 0.dp, current.onBackground, CircleShape)
                                .clickable { seed = palette.hexKey }, contentAlignment = Alignment.Center) {
                                if (seed == palette.hexKey) Icon(Icons.Outlined.Check, palette.name, tint = Color.White)
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppearanceToggle(stringResource(R.string.settings_follow_system_theme_title),
                            stringResource(R.string.follow_system_theme_description), !forceTheme) { forceTheme = !it }
                        AppearanceToggle(stringResource(R.string.settings_dark_mode_title),
                            stringResource(R.string.dark_mode_description), dark, enabled = forceTheme) { dark = it }
                        AppearanceToggle(stringResource(R.string.amoled_mode_title),
                            stringResource(R.string.amoled_mode_description), amoled) { amoled = it }
                    }
                }
                item {
                    Text(stringResource(R.string.gallery_appearance_effects), modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.titleLarge)
                }
                item {
                    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppearanceToggle(stringResource(R.string.fancy_blur), stringResource(R.string.gallery_appearance_blur),
                            blur, enabled = Build.VERSION.SDK_INT >= 31) { blur = it }
                        AppearanceToggle(stringResource(R.string.auto_contrast), stringResource(R.string.auto_contrast_description), contrast) { contrast = it }
                        AppearanceToggle(stringResource(R.string.shared_elements), stringResource(R.string.shared_elements_description), transitions) { transitions = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceToggle(title: String, summary: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.fillMaxWidth().clickable(enabled = enabled) { onChange(!checked) }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.4f))
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
        }
    }
}

/** Uses the current four-tab navigation and glass material, rather than the upstream mockup. */
@Composable
private fun GalleryAppearancePreview(modifier: Modifier) {
    val haze = rememberHazeState()
    val colors = MaterialTheme.colorScheme
    val navigation = rememberNavigationItems()
    CompositionLocalProvider(LocalHazeState provides haze) {
        Surface(modifier.fillMaxWidth().height(370.dp), shape = RoundedCornerShape(28.dp),
            color = colors.background, border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant)) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().hazeSource(haze)) {
                    Row(Modifier.fillMaxWidth().height(76.dp), horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Photo, null, Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.gallery_all_pictures), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(stringResource(R.string.header_today), Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { col ->
                                    Canvas(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(10.dp))) {
                                        val tint = listOf(colors.primary, colors.secondary, colors.tertiary)[(row + col) % 3]
                                        drawRect(tint.copy(alpha = 0.26f))
                                        drawCircle(tint.copy(alpha = 0.8f), size.width * 0.12f, Offset(size.width * 0.72f, size.height * 0.24f))
                                        val mountain = Path().apply {
                                            moveTo(0f, size.height); lineTo(size.width * 0.4f, size.height * 0.4f)
                                            lineTo(size.width, size.height); close()
                                        }
                                        drawPath(mountain, tint.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
                GalleryNavBar(Modifier.align(Alignment.BottomCenter), null, navigation, {}, selectedRoute = Screen.TimelineScreen())
            }
        }
    }
}
