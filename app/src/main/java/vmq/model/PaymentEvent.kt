package vmq.model

import java.math.BigDecimal
import java.math.RoundingMode

data class PaymentEvent(
    val type: PaymentType,
    val amount: Double,
) {
    // Yuan string with exactly two decimals, as the monitor API expects.
    val amountYuan: String
        get() = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toPlainString()
}
