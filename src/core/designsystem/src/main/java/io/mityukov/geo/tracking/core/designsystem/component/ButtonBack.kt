package io.mityukov.geo.tracking.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.core.designsystem.R
import io.mityukov.geo.tracking.core.test.AppTestTag

@Composable
fun ButtonBack(modifier: Modifier = Modifier, onBack: () -> Unit) {
    IconButton(modifier = modifier.testTag(AppTestTag.BUTTON_BACK), onClick = onBack) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.core_designsystem_content_description_back_button),
        )
    }
}
