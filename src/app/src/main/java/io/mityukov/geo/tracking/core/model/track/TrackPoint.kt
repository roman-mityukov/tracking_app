package io.mityukov.geo.tracking.core.model.track

import io.mityukov.geo.tracking.core.model.geo.Geolocation

data class TrackPoint(val id: String, val geolocation: Geolocation)
