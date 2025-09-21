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
import java.util.Locale

internal data class SpeedChartPoint(val distance: Int, val speed: Double)
internal data class SpeedChartData(val points: List<SpeedChartPoint>) {
    val maxDistance = points.maxBy { it.distance }.distance
    val maxSpeed = points.maxBy { it.speed }.speed
    val minSpeed = points.minBy { it.speed }.speed
}

@Composable
internal fun SpeedChart(
    modifier: Modifier = Modifier,
    chartData: SpeedChartData,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f)
    ) {
        Text(stringResource(R.string.feature_track_details_speed_chart_title))
        Spacer(modifier = Modifier.height(8.dp))
        SpeedChartCanvas(
            modifier = Modifier.fillMaxSize(),
            chartData = chartData,
        )
    }
}

@Composable
private fun SpeedChartCanvas(modifier: Modifier = Modifier, chartData: SpeedChartData) {
    val speedAxisLabel = stringResource(R.string.feature_track_details_speed_chart_axis_speed)
    val distanceAxisLabel = stringResource(R.string.feature_track_details_speed_chart_axis_distance)

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
                (size.height / (chartData.maxSpeed - chartData.minSpeed) * (chartData.maxSpeed - pair.speed)).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = Color.Blue, style = Stroke(width = 4f))

        drawVerticalAxisText(
            String.format(Locale.getDefault(), "%.1f", chartData.minSpeed),
            textStyle,
            textMeasurer,
            Offset(0f, size.height - measureResult.size.height),
        )
        drawVerticalAxisText(
            String.format(Locale.getDefault(), "%.1f", chartData.maxSpeed),
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
                speedAxisLabel,
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
private fun SpeedChartPreview(@PreviewParameter(SpeedChartDataProvider::class) data: SpeedChartData) {
    SpeedChart(modifier = Modifier.padding(0.dp), chartData = data)
}

private class SpeedChartDataProvider : PreviewParameterProvider<SpeedChartData> {
    override val values: Sequence<SpeedChartData> = sequenceOf(
        SpeedChartData(
            points = listOf(
                SpeedChartPoint(0, 1.5),
                SpeedChartPoint(100, 1.8),
                SpeedChartPoint(200, 1.4),
                SpeedChartPoint(300, 1.9),
                SpeedChartPoint(400, 1.8),
                SpeedChartPoint(500, 1.7),
                SpeedChartPoint(600, 1.1),
                SpeedChartPoint(700, 1.5),
                SpeedChartPoint(800, 1.4),
                SpeedChartPoint(900, 1.5),
                SpeedChartPoint(1000, 1.6),
            )
        )
    )
}
