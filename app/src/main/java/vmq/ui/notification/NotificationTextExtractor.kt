package vmq.ui.notification

object NotificationTextExtractor {
    fun extract(value: CharSequence?): String = value?.toString().orEmpty()
}
