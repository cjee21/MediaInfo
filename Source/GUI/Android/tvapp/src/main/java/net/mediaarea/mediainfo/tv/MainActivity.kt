/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import androidx.tv.material3.surfaceColorAtElevation
import net.mediaarea.mediainfo.tv.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaInfoAppContainer()
        }
    }
}

@Composable
fun MediaInfoAppContainer() {
    val viewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)

    // Collect the state from the ViewModel in a lifecycle-aware way
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    AndroidTheme(isInDarkTheme = isDark) {
        MediaInfoApp()
    }
}

@Composable
fun MediaInfoApp(drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Open)) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RectangleShape
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val items =
            listOf(
                "Browse" to Icons.Default.Folder,
                "Settings" to Icons.Default.Settings,
                "About" to Icons.Default.Info,
            )

        NavigationDrawer(
            drawerState = drawerState,
            drawerContent = { drawerValue ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp))
                        .fillMaxHeight()
                        .padding(12.dp)
                        .selectableGroup(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                ) {
                    items.forEach { (text, icon) ->
                        val route = text
                        NavigationDrawerItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    // Pop up to start destination to avoid building up a large stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                )
                            }
                        ) {
                            Text(text)
                        }
                    }
                }
                if (drawerValue == DrawerValue.Open) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
        ) {
            NavHost(navController, startDestination = "Browse") {
                composable("Browse") { Browse() }
                composable("Settings") { Settings() }
                composable("About") { About() }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun MediaInfoAppAppPreview() {
    AndroidTheme {
        MediaInfoApp()
    }
}

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun MediaInfoAppAppPreviewDrawerClosed() {
    AndroidTheme {
        MediaInfoApp(rememberDrawerState(initialValue = DrawerValue.Closed))
    }
}