package io.mityukov.geo.tracking.core.data.repository

sealed interface RepositoryFailure {
    data object IO : RepositoryFailure
}
