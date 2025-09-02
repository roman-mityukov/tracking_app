package io.mityukov.geo.tracking.feature.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.mityukov.geo.tracking.R
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.utils.test.AppTestTag

@Composable
fun MapControls(
    modifier: Modifier = Modifier,
    currentGeolocation: Geolocation?,
    onNavigateTo: (Geolocation) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    val mapNavigateTo = {
        if (currentGeolocation != null) {
            onNavigateTo(currentGeolocation)
        }
    }

    LaunchedEffect(currentGeolocation != null) {
        mapNavigateTo()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        MapControlButton(
            modifier = Modifier.testTag(AppTestTag.BUTTON_ZOOM_IN),
            onClick = onZoomIn,
            icon = R.drawable.icon_plus,
            contentDescription = stringResource(R.string.content_description_map_zoom_in),
        )
        Spacer(modifier = Modifier.height(8.dp))
        MapControlButton(
            modifier = Modifier.testTag(AppTestTag.BUTTON_ZOOM_OUT),
            onClick = onZoomOut,
            icon = R.drawable.icon_minus,
            contentDescription = stringResource(R.string.content_description_map_zoom_out),
        )
        Spacer(modifier = Modifier.height(8.dp))
        MapControlButton(
            modifier = Modifier.testTag(AppTestTag.BUTTON_NAVIGATE_TO_LOCATION),
            onClick = mapNavigateTo,
            icon = R.drawable.icon_my_location,
            contentDescription = stringResource(R.string.content_description_map_my_location),
        )
    }
}

@Composable
private fun MapControlButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Int,
    contentDescription: String,
) {
    Button(
        modifier = modifier.size(48.dp),
        onClick = onClick,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription)
    }
}

@Preview
@Composable
fun MapControlsPreview() {
    MapControls(
        onNavigateTo = { _ -> },
        onZoomIn = {},
        onZoomOut = {},
        currentGeolocation = null,
    )
}

@Preview
@Composable
fun MapControlButtonPreview() {
    MapControlButton(
        onClick = {},
        icon = R.drawable.icon_my_location,
        contentDescription = "",
    )
}
