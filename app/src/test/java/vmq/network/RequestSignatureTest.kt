package vmq.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vmq.util.HashUtils

class RequestSignatureTest {
    @Test
    fun `request signature matches cross language fixed vector`() {
        val body = "{\"channel\":\"alipay\",\"amount\":\"12.34\"}".toByteArray(Charsets.UTF_8)
        val canonicalRequest = """
            1784194689
            123e4567-e89b-42d3-a456-426614174000
            POST
            /api/monitor/payments
            1090d8540a48a68e900db69ccdf3f2a068b1e2ddc7242fd62d6e916b0411dffd
        """.trimIndent()

        assertEquals(
            "1090d8540a48a68e900db69ccdf3f2a068b1e2ddc7242fd62d6e916b0411dffd",
            HashUtils.sha256Hex(body),
        )
        assertEquals(
            canonicalRequest,
            RequestSignature.buildCanonicalRequest(
                method = "post",
                path = "/api/monitor/payments",
                timestamp = "1784194689",
                nonce = "123e4567-e89b-42d3-a456-426614174000",
                body = body,
            ),
        )
        assertEquals(
            "587d2c827305b96256fb9243cb2f3a8d419f50d3c03f42156fec668065c39599",
            RequestSignature.sign(
                method = "POST",
                path = "/api/monitor/payments",
                timestamp = "1784194689",
                nonce = "123e4567-e89b-42d3-a456-426614174000",
                body = body,
                key = "test_secret_key_123",
            ),
        )
    }

    @Test
    fun `heartbeat signature matches cross language fixed vector`() {
        val signature = RequestSignature.sign(
            method = "POST",
            path = "/api/monitor/heartbeat",
            timestamp = "1784194689",
            nonce = "123e4567-e89b-42d3-a456-426614174000",
            body = "{}".toByteArray(Charsets.UTF_8),
            key = "test_secret_key_123",
        )

        assertEquals("42e0275dac55496624d86169ffc6f1610d1c689f2911ae9e660a29369b6a0d8e", signature)
    }

    @Test
    fun `header names use the CloudPay prefix`() {
        assertEquals("X-CloudPay-Timestamp", RequestSignature.TIMESTAMP_HEADER)
        assertEquals("X-CloudPay-Nonce", RequestSignature.NONCE_HEADER)
        assertEquals("X-CloudPay-Signature", RequestSignature.SIGNATURE_HEADER)
    }

    @Test
    fun `request path keeps pathname and query`() {
        assertEquals(
            "/api/monitor/payments?x=1",
            RequestSignature.requestPath("https://example.com/api/monitor/payments?x=1"),
        )
        assertEquals(
            "/api/monitor/heartbeat",
            RequestSignature.requestPath("https://example.com/api/monitor/heartbeat"),
        )
    }

    @Test
    fun `nonce factory creates distinct UUID version 4 values`() {
        val first = RequestSignature.newNonce()
        val second = RequestSignature.newNonce()

        assertTrue(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$").matches(first))
        assertNotEquals(first, second)
    }
}
