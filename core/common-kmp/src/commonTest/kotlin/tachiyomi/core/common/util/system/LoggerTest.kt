// KMK -->
package tachiyomi.core.common.util.system

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoggerTest {

    @Test
    fun `logcat delegates priority tag throwable and message`() {
        val previous = Logging.logger
        val recording = RecordingLogger()
        Logging.logger = recording
        try {
            val error = IllegalStateException("boom")
            Sample.logcat(LogPriority.ERROR, error) { "hello" }

            assertEquals(1, recording.entries.size)
            val entry = recording.entries.single()
            assertEquals(LogPriority.ERROR, entry.priority)
            assertEquals("Sample", entry.tag)
            assertEquals("hello", entry.message)
            assertEquals(error, entry.throwable)
        } finally {
            Logging.logger = previous
        }
    }

    @Test
    fun `explicit tag is prefixed to the message`() {
        val previous = Logging.logger
        val recording = RecordingLogger()
        Logging.logger = recording
        try {
            Sample.logcat(messageTag = "custom") { "hello" }

            assertEquals("[custom] hello", recording.entries.single().message)
            assertEquals("Sample", recording.entries.single().tag)
        } finally {
            Logging.logger = previous
        }
    }

    @Test
    fun `message is evaluated once`() {
        val previous = Logging.logger
        val recording = RecordingLogger()
        Logging.logger = recording
        try {
            var calls = 0
            Sample.logcat {
                calls++
                "hello"
            }

            assertEquals(1, calls)
            assertTrue(recording.entries.single().message.isNotEmpty())
        } finally {
            Logging.logger = previous
        }
    }

    private object Sample

    private class RecordingLogger : Logger {
        val entries = mutableListOf<Entry>()

        override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: String) {
            entries.add(Entry(priority, tag, throwable, message))
        }
    }

    private data class Entry(
        val priority: LogPriority,
        val tag: String?,
        val throwable: Throwable?,
        val message: String,
    )
}
// KMK <--
