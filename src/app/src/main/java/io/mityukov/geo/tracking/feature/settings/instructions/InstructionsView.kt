package io.mityukov.geo.tracking.feature.settings.instructions

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.R

@Composable
fun InstructionsView(onInstructionsSelected: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onInstructionsSelected()
        },
        headlineContent = {
            Text(text = stringResource(R.string.instructions_view_label))
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    )
}
