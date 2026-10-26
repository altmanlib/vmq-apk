package vmq.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUrlBuilderTest {
    @Test
    fun `build adds http scheme when missing`() {
        val result = ApiUrlBuilder.build("example.com:8080", "api/monitor/heartbeat")

        assertEquals("http://example.com:8080/api/monitor/heartbeat", result)
    }

    @Test
    fun `build preserves https scheme`() {
        val result = ApiUrlBuilder.build("https://example.com", "api/monitor/heartbeat")

        assertEquals("https://example.com/api/monitor/heartbeat", result)
    }

    @Test
    fun `build trims trailing slashes from host`() {
        val result = ApiUrlBuilder.build("https://example.com///", "api/monitor/heartbeat")

        assertEquals("https://example.com/api/monitor/heartbeat", result)
    }

    @Test
    fun `build payment report url keeps path`() {
        val result = ApiUrlBuilder.buildPaymentReportUrl(
            hostValue = "https://example.com/api",
        )

        assertEquals(
            "https://example.com/api/api/monitor/payments",
            result,
        )
    }
}
