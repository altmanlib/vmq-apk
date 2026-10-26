package vmq.model

enum class PaymentType(val channel: String) {
    WECHAT("wechat"),
    ALIPAY("alipay"),
}
