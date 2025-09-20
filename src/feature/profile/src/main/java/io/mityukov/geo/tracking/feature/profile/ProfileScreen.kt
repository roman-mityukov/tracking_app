@file:Suppress("UnusedParameter")

package io.mityukov.geo.tracking.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.mityukov.geo.tracking.core.test.AppTestTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileScreen(
    onSettingsSelected: () -> Unit,
    onAboutSelected: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.feature_profile_title)) })
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .consumeWindowInsets(paddingValues)
        ) {
            ProfileScreenItem(
                Modifier
                    .clickable { onSettingsSelected() }
                    .testTag(AppTestTag.BUTTON_SETTINGS),
                stringResource(R.string.feature_profile_settings_label),
            )
            ProfileScreenItem(
                Modifier
                    .clickable { onAboutSelected() }
                    .testTag(AppTestTag.BUTTON_ABOUT),
                stringResource(R.string.feature_profile_about_label),
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
                contentDescription = stringResource(R.string.feature_profile_content_description_arrow_right)
            )
        }
    )
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen({}, {})
}
