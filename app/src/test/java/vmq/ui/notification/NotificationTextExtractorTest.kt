package vmq.ui.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTextExtractorTest {
    @Test
    fun `extract converts non string char sequence to text`() {
        val result = NotificationTextExtractor.extract(StringBuilder("个人收款码到账¥0.03"))

        assertEquals("个人收款码到账¥0.03", result)
    }
}
