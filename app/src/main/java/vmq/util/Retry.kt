package vmq.util

import kotlinx.coroutines.delay
import kotlin.math.min

/**
 * Retries a suspend block with exponential backoff.
 *
 * Only [RetryableException] triggers another attempt. All other exceptions
 * propagate immediately.
 */
suspend fun <T> retry(
    config: RetryConfig = RetryConfig(),
    block: suspend (attempt: Int) -> T,
): T {
    var lastException: RetryableException? = null

    for (attempt in 0 until config.maxAttempts) {
        try {
            return block(attempt)
        } catch (error: RetryableException) {
            lastException = error
            if (attempt < config.maxAttempts - 1) {
                delay(config.delayFor(attempt))
            }
        }
    }

    throw checkNotNull(lastException)
}

data class RetryConfig(
    val maxAttempts: Int = 4,
    val initialDelayMs: Long = 1_000L,
    val maxDelayMs: Long = 10_000L,
) {
    init {
        require(maxAttempts > 0) { "maxAttempts must be positive" }
        require(initialDelayMs >= 0) { "initialDelayMs must not be negative" }
        require(maxDelayMs >= 0) { "maxDelayMs must not be negative" }
    }

    fun delayFor(attempt: Int): Long =
        min(initialDelayMs * (1L shl attempt), maxDelayMs)
}

/** Thrown by a caller to signal that an operation should be retried. */
class RetryableException(message: String, cause: Throwable? = null) : Exception(message, cause)
