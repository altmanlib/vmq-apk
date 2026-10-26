package vmq.network

import java.util.UUID
import okhttp3.HttpUrl.Companion.toHttpUrl
import vmq.util.HashUtils

object RequestSignature {
    const val TIMESTAMP_HEADER = "X-CloudPay-Timestamp"
    const val NONCE_HEADER = "X-CloudPay-Nonce"
    const val SIGNATURE_HEADER = "X-CloudPay-Signature"

    fun newNonce(): String = UUID.randomUUID().toString()

    // Pathname plus query, as covered by the signature.
    fun requestPath(url: String): String {
        val httpUrl = url.toHttpUrl()
        return httpUrl.encodedPath + httpUrl.encodedQuery?.let { "?$it" }.orEmpty()
    }

    fun buildCanonicalRequest(
        method: String,
        path: String,
        timestamp: String,
        nonce: String,
        body: ByteArray,
    ): String = listOf(timestamp, nonce, method.uppercase(), path, HashUtils.sha256Hex(body)).joinToString("\n")

    fun sign(
        method: String,
        path: String,
        timestamp: String,
        nonce: String,
        body: ByteArray,
        key: String,
    ): String = HashUtils.hmacSha256Hex(
        buildCanonicalRequest(method, path, timestamp, nonce, body),
        key,
    )

    fun buildHeaders(
        method: String,
        path: String,
        timestamp: String,
        nonce: String,
        body: ByteArray,
        key: String,
    ): Map<String, String> = mapOf(
        TIMESTAMP_HEADER to timestamp,
        NONCE_HEADER to nonce,
        SIGNATURE_HEADER to sign(method, path, timestamp, nonce, body, key),
    )
}
