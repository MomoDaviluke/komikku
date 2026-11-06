package tachiyomi.core.common.util.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Think twice before using this. This is a delicate API. It is easy to accidentally create resource or memory leaks when GlobalScope is used.
 *
 * **Possible replacements**
 * - suspend function
 * - custom scope like view or presenter scope
 */
@DelicateCoroutinesApi
fun launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)

/**
 * Think twice before using this. This is a delicate API. It is easy to accidentally create resource or memory leaks when GlobalScope is used.
 *
 * **Possible replacements**
 * - suspend function
 * - custom scope like view or presenter scope
 */
@DelicateCoroutinesApi
fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, block)

/**
 * Think twice before using this. This is a delicate API. It is easy to accidentally create resource or memory leaks when GlobalScope is used.
 *
 * **Possible replacements**
 * - suspend function
 * - custom scope like view or presenter scope
 */
@DelicateCoroutinesApi
fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
    GlobalScope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, block)

fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.Main, block = block)

// KMK -->
// IO / NonCancellable helpers moved to :core:common-kmp for KMP code.
// KMK <--

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) = withContext(
    Dispatchers.Main,
    block,
)
