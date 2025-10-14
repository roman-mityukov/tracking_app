package io.mityukov.geo.tracking.core.data.validation

import io.mityukov.geo.tracking.core.model.track.Track
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class TrackValidatorTest {
    private val validTrack = Track(
        id = "trackEntityId",
        description = "description",
        name = "trackEntityName",
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
        filePath = "trackEntityFilePath",
    )
    private val validator = TrackValidatorImpl()

    @Test
    fun `validate valid track returns TrackValidationResult Valid`() {
        assert(validator.validate(validTrack) is TrackValidationResult.Valid)
        assert(
            validator.validate(
                validTrack.copy(
                    name = "",
                    description = ""
                )
            ) is TrackValidationResult.Valid
        )
        assert(
            validator.validate(
                validTrack.copy(
                    name = "A".repeat(TrackProperties.MAX_LENGTH_NAME),
                    description = "B".repeat(TrackProperties.MAX_LENGTH_DESCRIPTION)
                )
            ) is TrackValidationResult.Valid
        )
    }

    @Test
    fun `validate track with invalid name returns TrackValidationResult Invalid Name`() {
        assert(
            validator.validate(
                validTrack.copy(name = "A".repeat(TrackProperties.MAX_LENGTH_NAME + 1))
            ) is TrackValidationResult.Invalid.Name
        )
    }

    @Test
    fun `validate track with invalid description returns TrackValidationResult Invalid Description`() {
        assert(
            validator.validate(
                validTrack.copy(description = "B".repeat(TrackProperties.MAX_LENGTH_DESCRIPTION + 1))
            ) is TrackValidationResult.Invalid.Description
        )
    }
}
