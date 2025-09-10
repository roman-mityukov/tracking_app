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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.mapkit.mapview.MapView
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import io.mityukov.geo.tracking.core.yandexmap.showTrack

@Composable
internal fun TrackDetailsMapScreen(
    viewModel: TrackDetailsMapViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    Scaffold(contentWindowInsets = WindowInsets.safeContent) { paddingValues ->
        val viewModelState = viewModel.stateFlow.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (viewModelState.value) {
                is TrackDetailsMapState.Data -> {
                    val track = (viewModelState.value as TrackDetailsMapState.Data).data
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
                                io.mityukov.geo.tracking.core.designsystem.R.string.content_description_back_button
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
}
