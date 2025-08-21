package io.mityukov.geo.tracking.app.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.R

@Composable
fun ButtonBack(modifier: Modifier = Modifier, onBack: () -> Unit) {
    IconButton(modifier = modifier, onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.content_description_back_button),
        )
    }
}
