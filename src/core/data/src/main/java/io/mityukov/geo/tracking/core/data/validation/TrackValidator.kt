package io.mityukov.geo.tracking.core.data.validation

import io.mityukov.geo.tracking.core.model.track.Track

data object TrackProperties {
    const val MAX_LENGTH_NAME = 128
    const val MAX_LENGTH_DESCRIPTION = 512
}

sealed interface TrackValidationResult {
    data object Valid : TrackValidationResult
    sealed interface Invalid : TrackValidationResult {
        data object Name : Invalid
        data object Description : Invalid
    }
}

interface TrackValidator {
    fun validate(track: Track): TrackValidationResult
}
