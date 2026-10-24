/*
 * SPDX-FileCopyrightText: 2023-2026 IacobIacob01
 * SPDX-License-Identifier: Apache-2.0
 */

package com.dot.gallery.core.presentation.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dot.gallery.R
import com.dot.gallery.core.Constants.Animation.enterAnimation
import com.dot.gallery.core.Constants.Animation.exitAnimation
import com.dot.gallery.core.Settings.Misc.rememberAllowBlur
import com.dot.gallery.core.Settings.Misc.rememberAutoHideNavBar
import com.dot.gallery.core.Settings.Misc.rememberOldNavbar
import com.dot.gallery.feature_node.presentation.util.LocalHazeState
import com.dot.gallery.feature_node.presentation.util.NavigationItem
import com.dot.gallery.feature_node.presentation.util.Screen
import com.dot.gallery.ui.core.icons.Albums
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect

@Composable
fun rememberNavigationItems(): List<NavigationItem> {
    val timelineTitle = stringResource(R.string.gallery_photos)
    val albumsTitle = stringResource(R.string.nav_albums)
    val searchTitle = stringResource(R.string.search)
    val binTitle = stringResource(R.string.gallery_bin)
    return remember(timelineTitle, albumsTitle, searchTitle, binTitle) {
        mutableListOf(
            NavigationItem(
                name = timelineTitle,
                route = Screen.TimelineScreen.route,
                icon = Icons.Outlined.Photo,
            ),
            NavigationItem(
                name = albumsTitle,
                route = Screen.AlbumsScreen.route,
                icon = com.dot.gallery.ui.core.Icons.Albums,
            ),
            NavigationItem(
                name = searchTitle,
                route = Screen.SearchScreen(),
                icon = Icons.Outlined.Search
            ),
            NavigationItem(
                name = binTitle,
                route = Screen.TrashedScreen(),
                icon = Icons.Outlined.Delete
            )
        )
    }
}

/** Library stays reachable as a secondary browsing page after moving it to the menu. */
internal fun isGalleryBrowseRoute(route: String?, items: List<NavigationItem>): Boolean =
    items.any { it.route == route } || route == Screen.LibraryScreen()

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Stable
@Composable
fun AppBarContainer(
    navController: NavController,
    bottomBarState: Boolean,
    paddingValues: PaddingValues,
    isScrolling: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val bottomNavItems = rememberNavigationItems()
    val useNavRail by remember(windowSizeClass) {
        mutableStateOf(windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact)
    }
    val useOldNavbar by rememberOldNavbar()

    AnimatedVisibility(
        visible = useOldNavbar,
        enter = enterAnimation,
        exit = exitAnimation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val anySelectedRoute = remember(backStackEntry) {
                isGalleryBrowseRoute(navController.currentDestination?.route, bottomNavItems)
            }
            val showNavRail by remember(useNavRail, bottomBarState, anySelectedRoute) {
                derivedStateOf {
                    useNavRail && bottomBarState && anySelectedRoute
                }
            }
            AnimatedVisibility(
                visible = showNavRail,
                enter = slideInHorizontally { it * -2 },
                exit = slideOutHorizontally { it * -2 }
            ) {
                ClassicNavigationRail(
                    backStackEntry = backStackEntry,
                    navigationItems = bottomNavItems,
                    onClick = { navigate(navController, it) }
                )
            }
            val animatedPadding by animateDpAsState(
                targetValue = remember(showNavRail) {
                    if (showNavRail) 80.dp else 0.dp
                },
                label = "animatedPadding"
            )
            Box(
                modifier = Modifier.padding(start = animatedPadding)
            ) {
                content()
            }
            val hideNavBarSetting by rememberAutoHideNavBar()
            val showClassicNavbar by remember(useNavRail, bottomBarState, isScrolling, hideNavBarSetting, anySelectedRoute) {
                derivedStateOf {
                    !useNavRail && bottomBarState && (!isScrolling || !hideNavBarSetting) && anySelectedRoute
                }
            }
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = showClassicNavbar,
                enter = slideInVertically { it * 2 },
                exit = slideOutVertically { it * 2 },
                content = {
                    ClassicNavBar(
                        backStackEntry = backStackEntry,
                        navigationItems = bottomNavItems,
                        onClick = { navigate(navController, it) },
                    )
                }
            )
        }
    }
    AnimatedVisibility(
        visible = !useOldNavbar,
        enter = enterAnimation,
        exit = exitAnimation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            val hideNavBarSetting by rememberAutoHideNavBar()
            val anySelectedRoute = remember(backStackEntry) {
                isGalleryBrowseRoute(navController.currentDestination?.route, bottomNavItems)
            }
            val showNavbar by remember(bottomBarState, isScrolling, hideNavBarSetting, anySelectedRoute) {
                derivedStateOf {
                    bottomBarState && (!isScrolling || !hideNavBarSetting) && anySelectedRoute
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = paddingValues.calculateBottomPadding()),
                visible = showNavbar,
                enter = slideInVertically { it * 2 },
                exit = slideOutVertically { it * 2 },
                content = {
                    val modifier = remember(useNavRail) {
                        if (useNavRail) Modifier.requiredWidth((110 * bottomNavItems.size).dp)
                        else Modifier.fillMaxWidth()
                    }
                    GalleryNavBar(
                        modifier = modifier,
                        backStackEntry = backStackEntry,
                        navigationItems = bottomNavItems,
                        onClick = { navigate(navController, it) }
                    )
                }
            )
        }
    }
}

private fun navigate(navController: NavController, route: String) {
    navController.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }
}

@Composable
fun GalleryNavBar(
    modifier: Modifier,
    backStackEntry: NavBackStackEntry?,
    navigationItems: List<NavigationItem>,
    onClick: (route: String) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(6.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigationItems.forEach { item ->
            GalleryNavBarItem(
                navItem = item,
                isSelected = item.route == backStackEntry?.destination?.route,
                onClick = onClick,
            )
        }
    }
}

@Stable
@Composable
private fun Label(item: NavigationItem) = Text(
    text = item.name,
    fontWeight = FontWeight.Medium,
    style = MaterialTheme.typography.bodyMedium,
)

@Stable
@Composable
private fun Icon(item: NavigationItem) = Icon(
    imageVector = item.icon,
    contentDescription = item.name,
)

@Composable
fun ClassicNavBar(
    backStackEntry: NavBackStackEntry?,
    navigationItems: List<NavigationItem>,
    onClick: (route: String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        navigationItems.forEach { item ->
            val selected = item.route == backStackEntry?.destination?.route
            NavigationBarItem(
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                onClick = {
                    if (!selected) {
                        onClick(item.route)
                    }
                },
                label = { Label(item) },
                icon = { Icon(item) }
            )
        }
    }
}

@Composable
private fun ClassicNavigationRail(
    backStackEntry: NavBackStackEntry?,
    navigationItems: List<NavigationItem>,
    onClick: (route: String) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.weight(1f))
        navigationItems.forEach { item ->
            val selected = item.route == backStackEntry?.destination?.route
            NavigationRailItem(
                selected = selected,
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                onClick = {
                    if (!selected) {
                        onClick(item.route)
                    }
                },
                label = { Label(item) },
                icon = { Icon(item) }
            )
        }
        Spacer(Modifier.weight(1f))
    }

}

@Composable
fun RowScope.GalleryNavBarItem(
    navItem: NavigationItem,
    isSelected: Boolean,
    onClick: (route: String) -> Unit,
) {
    val background by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "navigationBackground",
    )
    val foreground by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navigationForeground",
    )
    Column(
        modifier = Modifier.weight(1f).height(72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .selectable(selected = isSelected, role = Role.Tab) {
                if (!isSelected) onClick(navItem.route)
            }.padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        Icon(navItem.icon, contentDescription = null, tint = foreground, modifier = Modifier.size(28.dp))
        Text(navItem.name, color = foreground, style = MaterialTheme.typography.labelLarge,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
