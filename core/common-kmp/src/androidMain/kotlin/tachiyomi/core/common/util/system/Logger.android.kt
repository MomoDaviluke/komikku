// KMK -->
package tachiyomi.core.common.util.system

import android.util.Log

actual fun defaultLogger(): Logger = AndroidLogger

private object AndroidLogger : Logger {

    override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: String) {
        val text = buildString {
            append(message)
            if (throwable != null) {
                if (message.isNotBlank()) append("\n")
                append(throwable.stackTraceToString())
            }
        }
        try {
            Log.println(priority.toAndroidLogPriority(), tag ?: "Komikku", text)
        } catch (_: RuntimeException) {
            // Android unit tests use an unmocked android.util.Log stub; keep logging non-fatal.
            System.err.println("${priority.name}/${tag ?: "Komikku"}: $text")
        }
    }
}

private fun LogPriority.toAndroidLogPriority(): Int = when (this) {
    LogPriority.VERBOSE -> Log.VERBOSE
    LogPriority.DEBUG -> Log.DEBUG
    LogPriority.INFO -> Log.INFO
    LogPriority.WARN -> Log.WARN
    LogPriority.ERROR -> Log.ERROR
    LogPriority.ASSERT -> Log.ASSERT
}
// KMK <--
