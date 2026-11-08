// KMK -->
package tachiyomi.core.common.util.system

/**
 * Platform-neutral wall clock, in milliseconds since the Unix epoch.
 *
 * Survives Kotlin/Native compilation: commonMain must not reference `java.lang.System` directly.
 */
expect fun currentTimeMillis(): Long
// KMK <--