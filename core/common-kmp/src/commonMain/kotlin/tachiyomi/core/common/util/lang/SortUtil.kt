// KMK -->
package tachiyomi.core.common.util.lang

/**
 * Locale-aware string comparison. Android/JVM use `java.text.Collator`; iOS will use
 * `NSString.compare` with the current locale (documented as approximate parity).
 */
expect fun String.compareToWithCollator(other: String): Int
// KMK <--
