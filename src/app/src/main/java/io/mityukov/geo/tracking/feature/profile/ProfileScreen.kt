@file:Suppress("UnusedParameter")
package io.mityukov.geo.tracking.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onStatisticsSelected: () -> Unit,
    onSettingsSelected: () -> Unit,
    onAboutSelected: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.profile_title)) })
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            )
        ) {
//            ProfileScreenItem(
//                Modifier.clickable { onStatisticsSelected() },
//                stringResource(R.string.profile_statistics_label),
//            )
            ProfileScreenItem(
                Modifier.clickable { onSettingsSelected() },
                stringResource(R.string.profile_settings_label),
            )
            ProfileScreenItem(
                Modifier.clickable { onAboutSelected() },
                stringResource(R.string.profile_about_label),
            )
        }
    }
}

@Composable
private fun ProfileScreenItem(modifier: Modifier = Modifier, label: String) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(text = label)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.content_description_arrow_right)
            )
        }
    )
}
