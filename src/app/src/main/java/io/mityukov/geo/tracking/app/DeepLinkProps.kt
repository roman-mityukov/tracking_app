package io.mityukov.geo.tracking.app

object DeepLinkProps {
    const val APP_SCHEME: String = "geoapp"
    const val TRACK_DETAILS_HOST: String = "track"
    const val TRACK_DETAILS_PATH: String = "trackId"
    const val TRACK_DETAILS_URI_PATTERN: String =
        "$APP_SCHEME://$TRACK_DETAILS_HOST/{$TRACK_DETAILS_PATH}"
}
