// KMK -->
package tachiyomi.core.common.util

import tachiyomi.core.common.util.QuerySanitizer.sanitize
import kotlin.test.Test
import kotlin.test.assertEquals

class QuerySanitizerTest {

    @Test
    fun `trims separators and normalizes quotes`() {
        assertEquals("test", " --test-- ".sanitize())
        assertEquals("it's ok", " it’s ok ".sanitize())
    }
}
// KMK <--
