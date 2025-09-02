package io.mityukov.geo.tracking.utils.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

@Preview(
    name = "Small font",
    group = "font scales",
    fontScale = 0.5f
)
@Preview(
    name = "Large font",
    group = "font scales",
    fontScale = 1.5f
)
annotation class FontScalePreviews

@Preview(
    name = "NightMode",
    group = "ui_mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF000000,
    showBackground = true
)
annotation class NightModePreview

@Preview(
    name = "Green dominated",
    group = "wallpapers",
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
annotation class WallpapersPreviews
