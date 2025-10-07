package io.mityukov.geo.tracking.feature.track.details

import io.mityukov.geo.tracking.core.model.track.Track
import kotlin.time.Duration.Companion.seconds

val track = Track(
    id = "trackDomainId",
    description = "trackDomainDescription",
    name = "trackDomainName",
    start = 123,
    end = 456,
    distance = 789.3f,
    altitudeUp = 3.3f,
    altitudeDown = 4.1f,
    duration = 123L.seconds,
    sumSpeed = 24.5f,
    minSpeed = 12.1f,
    maxSpeed = 13.2f,
    geolocationCount = 3,
    filePath = "trackDomainFilePath",
)
