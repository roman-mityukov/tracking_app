package io.mityukov.geo.tracking.core.ui

import android.text.format.DateUtils
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.mityukov.geo.tracking.core.designsystem.icon.AppIcons
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration

@Composable
fun TrackProperties(
    modifier: Modifier = Modifier,
    duration: Duration,
    distance: Float,
    altitudeUp: Float,
    altitudeDown: Float,
    speed: Float,
) {
    FlowRow(modifier = modifier) {
        TrackItemProperty(
            imageVector = AppIcons.Duration,
            text = DateUtils.formatElapsedTime(
                duration.inWholeSeconds
            ),
            contentDescription = stringResource(R.string.content_description_track_time),
        )
        TrackItemProperty(
            imageVector = AppIcons.Distance,
            text = "${distance.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_distance),
        )
        TrackItemProperty(
            imageVector = AppIcons.AltitudeUp,
            text = "${altitudeUp.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_altitude_up),
        )
        TrackItemProperty(
            imageVector = AppIcons.AltitudeDown,
            text = "${altitudeDown.roundToInt()}м",
            contentDescription = stringResource(R.string.content_description_track_altitude_down),
        )
        TrackItemProperty(
            imageVector = AppIcons.Speed,
            text = "${String.format(Locale.getDefault(), "%.2f", speed)}м/с",
            contentDescription = stringResource(R.string.content_description_track_average_speed),
        )
    }
}

@Composable
fun TrackItemProperty(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    contentDescription: String
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = imageVector,
            contentDescription = contentDescription
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, overflow = TextOverflow.Clip)
        Spacer(modifier = Modifier.width(16.dp))
    }
}
