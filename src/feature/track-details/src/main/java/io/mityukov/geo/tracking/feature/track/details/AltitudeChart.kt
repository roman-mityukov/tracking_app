@file:Suppress("UnusedPrivateMember")

package io.mityukov.geo.tracking.feature.track.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.mityukov.geo.tracking.core.ui.FontScalePreviews
import io.mityukov.geo.tracking.core.ui.NightModePreview

internal data class ChartPoint(val distance: Int, val altitude: Int)
internal data class AltitudeChartData(val points: List<ChartPoint>) {
    val maxDistance = points.maxBy { it.distance }.distance
    val maxAltitude = points.maxBy { it.altitude }.altitude
    val minAltitude = points.minBy { it.altitude }.altitude
}

@Composable
internal fun AltitudeChart(
    modifier: Modifier = Modifier,
    chartData: AltitudeChartData,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f)
    ) {
        Text(stringResource(R.string.feature_track_details_altitude_chart_title))
        Spacer(modifier = Modifier.height(8.dp))
        AltitudeChartCanvas(
            modifier = Modifier.fillMaxSize(),
            chartData = chartData,
        )
    }
}

@Composable
private fun AltitudeChartCanvas(modifier: Modifier = Modifier, chartData: AltitudeChartData) {
    val altitudeAxisLabel = stringResource(R.string.feature_track_details_altitude_chart_axis_altitude)
    val distanceAxisLabel = stringResource(R.string.feature_track_details_altitude_chart_axis_distance)

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer(cacheSize = 0)
    val isDarkTheme = isSystemInDarkTheme()
    val axisColor = if (isDarkTheme) Color.White else Color.Black
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = axisColor)

    val measureResult =
        textMeasurer.measure(text = chartData.maxDistance.toString(), style = textStyle)
    val padding = with(density) {
        measureResult.size.height.toDp()
    }

    Canvas(
        modifier = modifier.padding(start = padding, bottom = padding)
    ) {
        drawLine(color = axisColor, start = Offset(0f, size.height), end = Offset(0f, 0f))
        drawLine(
            color = axisColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height)
        )

        val path = Path()
        chartData.points.forEachIndexed { index, pair ->
            val x = size.width / chartData.maxDistance * pair.distance
            val y =
                size.height / (chartData.maxAltitude - chartData.minAltitude) * (chartData.maxAltitude - pair.altitude)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = Color.Blue, style = Stroke(width = 4f))

        drawVerticalAxisText(
            chartData.minAltitude.toString(),
            textStyle,
            textMeasurer,
            Offset(0f, size.height - measureResult.size.height),
        )
        drawVerticalAxisText(
            chartData.maxAltitude.toString(),
            textStyle,
            textMeasurer,
            Offset(0f, 0f),
        )

        drawVerticalAxisText("0", textStyle, textMeasurer, Offset(0f, size.height))
        drawVerticalAxisText(
            chartData.maxDistance.toString(),
            textStyle,
            textMeasurer,
            Offset(size.width - measureResult.size.width, size.height),
        )

        rotate(degrees = 90f, pivot = Offset(0f, size.height / 2)) {
            drawHorizontalAxisText(
                altitudeAxisLabel,
                textStyle,
                textMeasurer,
                Offset(0f, size.height / 2),
            )
        }
        drawHorizontalAxisText(
            distanceAxisLabel,
            textStyle,
            textMeasurer,
            Offset(size.width / 2, size.height),
        )
    }
}

private fun DrawScope.drawVerticalAxisText(
    text: String,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    center: Offset,
) {
    val measureResult = textMeasurer.measure(text = text, style = textStyle)
    val topLeft = center
    drawText(textLayoutResult = measureResult, topLeft = topLeft)
}

private fun DrawScope.drawHorizontalAxisText(
    text: String,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    center: Offset,
) {
    val measureResult = textMeasurer.measure(text = text, style = textStyle)
    val topLeft = Offset(center.x - measureResult.size.width / 2, center.y)
    drawText(textLayoutResult = measureResult, topLeft = topLeft)
}

@FontScalePreviews
@NightModePreview
@Preview(name = "Main")
@Composable
private fun AltitudeChartPreview(@PreviewParameter(AltitudeChartDataProvider::class) data: AltitudeChartData) {
    AltitudeChart(modifier = Modifier.padding(0.dp), chartData = data)
}

private class AltitudeChartDataProvider : PreviewParameterProvider<AltitudeChartData> {
    override val values: Sequence<AltitudeChartData> = sequenceOf(
        AltitudeChartData(
            points = listOf(
                ChartPoint(0, 300),
                ChartPoint(100, 250),
                ChartPoint(200, 268),
                ChartPoint(300, 270),
                ChartPoint(400, 280),
                ChartPoint(500, 300),
                ChartPoint(600, 280),
                ChartPoint(700, 270),
                ChartPoint(800, 300),
            )
        )
    )
}
