@file:Suppress("TooManyFunctions")

package io.mityukov.geo.tracking.feature.map

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.keepScreenOn
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.core.model.geo.Geolocation
import io.mityukov.geo.tracking.core.test.AppTestTag
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import io.mityukov.geo.tracking.core.ui.UiProps
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun MapContent(
    onUpdateCurrentLocation: (Geolocation) -> Unit,
    onSharing: () -> Unit,
    onPendingLocation: () -> Unit,
    onPendingLocationComplete: () -> Unit,
    mapViewFactory: (Context) -> View,
    mapViewModelState: MapState,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        MapAndroidView(
            mapViewFactory = mapViewFactory,
        )
        MapInfoContent(
            onUpdateCurrentLocation = onUpdateCurrentLocation,
            onSharing = onSharing,
            onPendingLocation = onPendingLocation,
            onPendingLocationComplete = onPendingLocationComplete,
            viewModelState = mapViewModelState,
        )
    }
}

@Composable
private fun MapAndroidView(
    modifier: Modifier = Modifier,
    mapViewFactory: (Context) -> View,
) {
    AndroidView(
        modifier = modifier.keepScreenOn(),
        factory = { context ->
            mapViewFactory(context)
        }
    )
}

@Composable
private fun MapInfoContent(
    modifier: Modifier = Modifier,
    viewModelState: MapState,
    onUpdateCurrentLocation: (Geolocation) -> Unit,
    onPendingLocation: () -> Unit,
    onPendingLocationComplete: () -> Unit,
    onSharing: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        when (viewModelState) {
            is MapState.CurrentLocation -> {
                LaunchedEffect(viewModelState.data) {
                    onPendingLocationComplete()
                    onUpdateCurrentLocation(viewModelState.data)
                }

                CurrentGeolocation(
                    modifier = modifier,
                    geolocation = viewModelState.data,
                    onShare = onSharing,
                )
            }

            MapState.PendingLocationUpdates -> {
                LaunchedEffect(viewModelState) {
                    onPendingLocation()
                }
            }

            is MapState.NoLocation -> {
                // Обработка происходит в MapPermissions
            }
        }
    }
}

@Composable
private fun CurrentGeolocation(
    modifier: Modifier = Modifier,
    geolocation: Geolocation,
    onShare: () -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                text = stringResource(
                    R.string.feature_map_current_location_message,
                    geolocation.localDateTime.format(UiProps.DEFAULT_DATE_TIME_FORMATTER),
                    geolocation.latitude,
                    geolocation.longitude,
                    geolocation.altitude.roundToInt(),
                    String.format(Locale.getDefault(), "%.1f", geolocation.speed),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            IconButton(
                modifier = Modifier.testTag(AppTestTag.BUTTON_SHARE_CURRENT_LOCATION),
                onClick = onShare
            ) {
                Icon(imageVector = AppIcons.Share, null)
            }
        }
    }
}

@Preview
@FontScalePreviews
@Composable
fun MapContentPreview() {
    MapContent(
        onUpdateCurrentLocation = { _ -> },
        onSharing = {},
        onPendingLocation = {},
        onPendingLocationComplete = {},
        mapViewFactory = { context -> View(context) },
        mapViewModelState = MapState.CurrentLocation(
            data = Geolocation(
                latitude = 53.654810,
                longitude = 87.450375,
                altitude = 310.2,
                speed = 1.4f,
                time = 1756964259,
            )
        )
    )
}
