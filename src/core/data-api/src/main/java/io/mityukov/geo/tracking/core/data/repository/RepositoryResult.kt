package io.mityukov.geo.tracking.core.data.repository

sealed class RepositoryResult<out T> {
    data class Success<out T>(val data: T) : RepositoryResult<T>()
    data class Failure<out E: RepositoryFailure>(val cause: E) : RepositoryResult<Nothing>()

    val isSuccess = this is Success<*>
    val isFailure = this is Failure<*>

    inline fun onSuccess(block: (T) -> Unit): RepositoryResult<T> {
        if (this is Success) block(data)
        return this
    }

    inline fun onFailure(block: (RepositoryFailure) -> Unit): RepositoryResult<T> {
        if (this is Failure<*>) block(cause)
        return this
    }
}
