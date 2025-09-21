package io.mityukov.geo.tracking.feature.settings.instructions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.mityukov.geo.tracking.core.designsystem.component.ButtonBack
import io.mityukov.geo.tracking.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InstructionsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.feature_settings_instructions_title)) },
                navigationIcon = {
                    ButtonBack(onBack = onBack)
                }
            )
        },
        contentWindowInsets = WindowInsets.safeContent
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            )
        ) {
            Text(
                text = stringResource(R.string.feature_settings_instructions_description),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.feature_settings_instructions_content),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
internal fun InstructionsScreenPreview() {
    InstructionsScreen {  }
}
