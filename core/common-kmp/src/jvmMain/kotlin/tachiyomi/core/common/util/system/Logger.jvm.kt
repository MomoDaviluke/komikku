// KMK -->
package tachiyomi.core.common.util.system

actual fun defaultLogger(): Logger = JvmLogger

private object JvmLogger : Logger {

    override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: String) {
        System.err.println("${priority.name}/${tag ?: "Komikku"}: $message")
        throwable?.printStackTrace()
    }
}
// KMK <--
