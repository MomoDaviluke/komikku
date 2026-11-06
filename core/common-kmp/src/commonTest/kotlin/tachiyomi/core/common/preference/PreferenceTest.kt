// KMK -->
package tachiyomi.core.common.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PreferenceTest {

    @Test
    fun `in-memory store round trip`() {
        val store = InMemoryPreferenceStore()
        val preference = store.getString("key", "default")

        assertEquals("default", preference.get())
        assertFalse(preference.isSet())

        preference.set("value")
        assertTrue(preference.isSet())
        assertEquals("value", preference.get())

        preference.delete()
        assertEquals("default", preference.get())
        assertFalse(preference.isSet())
    }

    @Test
    fun `in-memory string set round trip`() {
        val store = InMemoryPreferenceStore()
        val preference = store.getStringSet("set", setOf("default"))

        assertEquals(setOf("default"), preference.get())
        assertFalse(preference.isSet())

        preference.set(setOf("a", "b"))
        assertTrue(preference.isSet())
        assertEquals(setOf("a", "b"), preference.get())
    }

    @Test
    fun `tri state cycles through all values`() {
        assertEquals(TriState.ENABLED_IS, TriState.DISABLED.next())
        assertEquals(TriState.ENABLED_NOT, TriState.ENABLED_IS.next())
        assertEquals(TriState.DISABLED, TriState.ENABLED_NOT.next())
    }

    @Test
    fun `checkbox state cycles between checked and none`() {
        val checked = assertIs<CheckboxState.State.Checked<String>>(
            CheckboxState.State.Checked("value"),
        )
        assertEquals(CheckboxState.State.None("value"), checked.next())
        assertEquals(CheckboxState.State.Checked("value"), checked.next().next())
    }
}
// KMK <--
