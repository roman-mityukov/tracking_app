package io.mityukov.geo.tracking.core.data.validation

import io.mityukov.geo.tracking.core.model.track.Track
import javax.inject.Inject

class TrackValidatorImpl @Inject constructor() : TrackValidator {
    override fun validate(track: Track): TrackValidationResult {
        val results = listOf(
            validateName(track.name),
            validateDescription(track.description),
        )

        return if (results.all { it == TrackValidationResult.Valid }) {
            TrackValidationResult.Valid
        } else {
            results.first { it != TrackValidationResult.Valid }
        }
    }

    private fun validateName(name: String): TrackValidationResult {
        return if (name.length > TrackProperties.MAX_LENGTH_NAME) {
            TrackValidationResult.Invalid.Name
        } else {
            TrackValidationResult.Valid
        }
    }

    private fun validateDescription(description: String): TrackValidationResult {
        return if (description.length > TrackProperties.MAX_LENGTH_DESCRIPTION) {
            TrackValidationResult.Invalid.Description
        } else {
            TrackValidationResult.Valid
        }
    }
}
