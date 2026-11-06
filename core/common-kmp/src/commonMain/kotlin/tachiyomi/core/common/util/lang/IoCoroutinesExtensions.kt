// KMK -->
package tachiyomi.core.common.util.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.IO, block = block)

fun CoroutineScope.launchNonCancellable(block: suspend CoroutineScope.() -> Unit): Job =
    launchIO { withContext(NonCancellable, block) }

suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)

suspend fun <T> withNonCancellableContext(block: suspend CoroutineScope.() -> T): T =
    withContext(NonCancellable, block)
// KMK <--
