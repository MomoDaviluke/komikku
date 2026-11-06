// KMK -->
package tachiyomi.core.common.util.lang

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutinesExtensionsTest {

    @Test
    fun `withIOContext returns the block value`() = runTest {
        assertEquals(42, withIOContext { 42 })
    }

    @Test
    fun `withNonCancellableContext returns the block value`() = runTest {
        assertEquals("ok", withNonCancellableContext { "ok" })
    }
}
// KMK <--
