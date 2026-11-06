// KMK -->
package mihon.core.common.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class JsonObjectTest {

    @Test
    fun `empty json object has no keys`() {
        assertEquals(0, JsonObjectEmpty.size)
        assertEquals(0, JsonObjectEmptyBytes.size - 2)
    }
}
// KMK <--
