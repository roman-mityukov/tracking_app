package io.mityukov.geo.tracking.utils.ui

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
    name = "Green dominated",
    group = "wallpapers",
    wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE
)
annotation class WallpapersPreviews
