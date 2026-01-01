/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import net.mediaarea.mediainfo.tv.ui.theme.AndroidTheme

@Composable
fun Settings() {
    val viewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()
    SettingsContent(isDark = isDark) {
        viewModel.toggleTheme(it)
    }
}

@Composable
fun SettingsContent(
    isDark: Boolean,
    onClick: (Boolean) -> Unit
) {
    var isChecked = isDark
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium,
        )
        ListItem(
            selected = false,
            onClick = {
                isChecked = !isChecked
                onClick(isChecked)
            },
            headlineContent = { Text(stringResource(R.string.dark_mode)) },
            supportingContent = {
                Text(
                    if (isChecked)
                        stringResource(R.string.enabled)
                    else
                        stringResource(R.string.disabled)
                )
            },
            trailingContent = {
                Switch(
                    checked = isChecked,
                    onCheckedChange = null
                )
            }
        )
    }
}

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun SettingsContentPreviewDark() {
    val isDark = true
    AndroidTheme(isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape
        ) {
            SettingsContent(isDark = isDark) {}
        }
    }
}

@Preview(showBackground = true, device = "id:tv_4k")
@Composable
fun SettingsContentPreviewLight() {
    val isDark = false
    AndroidTheme(isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape
        ) {
            SettingsContent(isDark = isDark) {}
        }
    }
}