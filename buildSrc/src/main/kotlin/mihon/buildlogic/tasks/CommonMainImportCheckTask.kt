// KMK -->
package mihon.buildlogic.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Guards Kotlin Multiplatform `commonMain` sources against platform-only APIs.
 *
 * `commonMain` must stay compilable for every configured target (Android, JVM, and later
 * iOS/Native). JVM-only references still compile on the Android/JVM targets today and only
 * explode when a Native target is added, so they are rejected eagerly here.
 */
abstract class CommonMainImportCheckTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val commonMainDir: DirectoryProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun check() {
        val root = commonMainDir.get().asFile
        val violations = mutableListOf<String>()

        root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .sortedBy { it.invariantSeparatorsPath }
            .forEach { file ->
                val rawLines = file.readLines()
                // Strip comments first so documentation that mentions JVM types does not trip the gate.
                val codeLines = stripComments(rawLines.joinToString("\n")).lines()
                codeLines.forEachIndexed { index, line ->
                    val reason = when {
                        FORBIDDEN_IMPORT.containsMatchIn(line.trim()) -> "forbidden platform import"
                        JVM_SYSTEM_API.containsMatchIn(line) -> "JVM-only System API"
                        PLATFORM_FQCN.containsMatchIn(line) -> "fully-qualified platform type"
                        else -> null
                    }
                    if (reason != null) {
                        violations += "${file.relativeTo(root).invariantSeparatorsPath}:${index + 1}: " +
                            "$reason -> ${rawLines.getOrNull(index).orEmpty().trim()}"
                    }
                }
            }

        val report = reportFile.get().asFile.apply { parentFile.mkdirs() }
        if (violations.isEmpty()) {
            report.writeText("OK: no platform leaks in ${root.invariantSeparatorsPath}\n")
            logger.lifecycle("commonMain platform gate passed for ${root.invariantSeparatorsPath}")
            return
        }

        report.writeText(violations.joinToString(separator = "\n", postfix = "\n"))
        throw GradleException(
            "commonMain platform leak(s) detected in ${root.invariantSeparatorsPath}:\n" +
                violations.joinToString(separator = "\n") +
                "\nUse expect/actual, the PlatformFile abstraction or a platform-neutral dependency instead.",
        )
    }

    /** Replaces Kotlin comments with spaces, preserving line count so line numbers stay valid. */
    private fun stripComments(source: String): String {
        val out = StringBuilder(source.length)
        var index = 0
        var inBlockComment = false
        var inLineComment = false
        while (index < source.length) {
            val current = source[index]
            val next = source.getOrNull(index + 1)
            when {
                inLineComment -> {
                    if (current == '\n') {
                        inLineComment = false
                        out.append('\n')
                    } else {
                        out.append(' ')
                    }
                }
                inBlockComment -> {
                    if (current == '*' && next == '/') {
                        inBlockComment = false
                        out.append("  ")
                        index++
                    } else {
                        out.append(if (current == '\n') '\n' else ' ')
                    }
                }
                current == '/' && next == '/' -> {
                    inLineComment = true
                    out.append("  ")
                    index++
                }
                current == '/' && next == '*' -> {
                    inBlockComment = true
                    out.append("  ")
                    index++
                }
                else -> out.append(current)
            }
            index++
        }
        return out.toString()
    }

    private companion object {
        private val FORBIDDEN_IMPORT = Regex("^import\\s+(android|androidx|java|javax|kotlin\\.jvm)\\..*")
        private val JVM_SYSTEM_API =
            Regex("\\bSystem\\.(currentTimeMillis|nanoTime|getenv|getProperty|getProperties|exit|err|out)\\b")
        private val PLATFORM_FQCN =
            Regex("\\b(android|androidx|java|javax)\\.(app|content|graphics|lang|math|net|nio|os|text|time|util|view|widget)\\.[A-Z]")
    }
}
// KMK <--