// KMK -->
package tachiyomi.core.common.util.system

/**
 * Platform-neutral log priority. Mirrors `logcat.LogPriority` so existing call sites keep working
 * after the import migration.
 */
enum class LogPriority {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ASSERT,
}

fun interface Logger {
    fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: String)
}

/**
 * Replaced at app startup on platforms that need a custom sink (e.g. iOS `NSLog`).
 */
expect fun defaultLogger(): Logger

object Logging {
    var logger: Logger = defaultLogger()

    fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: String) {
        logger.log(priority, tag, throwable, message)
    }
}

/**
 * Library-compatible overload: the second positional argument is the actual log tag.
 * Kept so existing `logcat(LogPriority.X, tag) { ... }` call sites keep their semantics.
 */
inline fun Any.logcat(
    priority: LogPriority,
    tag: String?,
    message: () -> String,
) {
    Logging.log(priority, tag, null, message())
}

/**
 * Extension-compatible overload: [messageTag] is prefixed to the message (legacy Komikku
 * behavior), while the receiver's class name becomes the log tag.
 */
inline fun Any.logcat(
    priority: LogPriority = LogPriority.DEBUG,
    throwable: Throwable? = null,
    messageTag: String? = null,
    message: () -> String = { "" },
) {
    val text = buildString {
        if (!messageTag.isNullOrEmpty()) append("[$messageTag] ")
        append(message())
    }
    Logging.log(priority, this::class.simpleName, throwable, text)
}
// KMK <--
