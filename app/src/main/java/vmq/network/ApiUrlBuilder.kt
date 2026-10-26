package vmq.network

object ApiUrlBuilder {
    fun buildHeartBeatUrl(hostValue: String): String {
        return build(hostValue, "api/monitor/heartbeat")
    }

    fun buildPaymentReportUrl(hostValue: String): String {
        return build(hostValue, "api/monitor/payments")
    }

    fun build(hostValue: String, pathAndQuery: String): String {
        return normalizeHost(hostValue) + "/" + pathAndQuery
    }

    private fun normalizeHost(hostValue: String): String {
        val trimmedHost = trimTrailingSlash(hostValue.trim())
        return if (trimmedHost.startsWith("http://") || trimmedHost.startsWith("https://")) {
            trimmedHost
        } else {
            "http://$trimmedHost"
        }
    }

    private fun trimTrailingSlash(value: String): String {
        if (value.isEmpty()) {
            return ""
        }

        var trimmedValue = value.trim()
        while (trimmedValue.endsWith('/')) {
            trimmedValue = trimmedValue.dropLast(1)
        }
        return trimmedValue
    }
}
