// KMK -->
package tachiyomi.domain.release.interactor

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReleaseVersionTest {

    @Test
    fun stableVersionComparison() {
        assertTrue(isNewVersion(false, 0, "v0.1.2", "v0.1.3"))
        assertTrue(isNewVersion(false, 0, "v0.1.2", "v0.2"))
        assertTrue(isNewVersion(false, 0, "v0.2.0", "v0.10.0"))
        assertTrue(isNewVersion(false, 0, "v0.1.2", "0.1.3"))

        assertFalse(isNewVersion(false, 0, "v0.1.2", "v0.1.2"))
        assertFalse(isNewVersion(false, 0, "v0.1.2", "v0.1"))
        assertFalse(isNewVersion(false, 0, "v0.1.2", "latest"))
    }

    @Test
    fun previewVersionComparison() {
        assertTrue(isNewVersion(true, 1233, "r1234", "r1234"))
        assertFalse(isNewVersion(true, 1234, "r1234", "r1234"))
        assertFalse(isNewVersion(true, 1234, "r1234", "beta"))
    }
}
// KMK <--
