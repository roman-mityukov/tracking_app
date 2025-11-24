package io.mityukov.geo.tracking.feature.track.details

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.core.yandexmap.showTrack
import io.mityukov.geo.tracking.feature.track.details.navigation.TrackDetailsMapRoute
import io.mityukov.geo.tracking.core.designsystem.R as designSystemResources

@Composable
internal fun TrackDetailsMapPane(
    navKey: TrackDetailsMapRoute,
    onBack: () -> Unit,
) {
    val viewModel: TrackDetailsMapViewModel =
        hiltViewModel<TrackDetailsMapViewModel, TrackDetailsMapViewModel.Factory>(
            creationCallback = { factory -> factory.create(navKey) }
        )
    Scaffold(contentWindowInsets = WindowInsets.safeContent) { paddingValues ->
        val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()
        TrackDetailsMapContent(paddingValues, viewModelState.value, onBack)
    }
}

@Composable
private fun TrackDetailsMapContent(
    paddingValues: PaddingValues,
    viewModelState: TrackDetailsMapState,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (viewModelState) {
            TrackDetailsMapState.Failure -> {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.feature_track_details_read_data_failure)
                )
            }

            is TrackDetailsMapState.Data -> {
                val track = viewModelState.data
                val context = LocalContext.current
                val mapView = remember { MapView(context) }
                MapLifecycle(
                    onStart = {
                        mapView.onStart()
                    },
                    onStop = {
                        mapView.onStop()
                    },
                )
                AndroidView(
                    factory = { context ->
                        mapView.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        mapView
                    }
                )

                val startPadding =
                    if (paddingValues.calculateStartPadding(LayoutDirection.Ltr) == 0.dp) {
                        16.dp
                    } else {
                        paddingValues.calculateStartPadding(LayoutDirection.Ltr)
                    }
                Button(
                    modifier = Modifier
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            start = startPadding
                        )
                        .size(48.dp),
                    onClick = onBack,
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Back,
                        contentDescription = stringResource(
                            designSystemResources.string.core_designsystem_content_description_back_button
                        )
                    )
                }

                if (track.geolocations.isNotEmpty()) {
                    LaunchedEffect(track.geolocations.last()) {
                        mapView.showTrack(context, track.geolocations, true)
                    }
                }
            }

            TrackDetailsMapState.Pending -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTrackDetailsMapContent(
    @PreviewParameter(provider = TrackDetailsMapStateProvider::class) state: TrackDetailsMapState
) {
    TrackDetailsMapContent(PaddingValues.Zero, state, {})
}

private class TrackDetailsMapStateProvider : PreviewParameterProvider<TrackDetailsMapState> {
    override val values: Sequence<TrackDetailsMapState>
        get() = sequenceOf(
            TrackDetailsMapState.Failure,
            TrackDetailsMapState.Pending,
        )

}
