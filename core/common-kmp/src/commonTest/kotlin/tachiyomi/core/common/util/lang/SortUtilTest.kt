// KMK -->
package tachiyomi.core.common.util.lang

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortUtilTest {

    @Test
    fun `orders alphabetically`() {
        assertTrue("apple".compareToWithCollator("banana") < 0)
        assertTrue("banana".compareToWithCollator("apple") > 0)
        assertEquals(0, "apple".compareToWithCollator("apple"))
    }

    @Test
    fun `primary strength ignores case`() {
        assertEquals(0, "apple".compareToWithCollator("APPLE"))
    }
}
// KMK <--
