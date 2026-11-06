// KMK -->
package eu.kanade.tachiyomi.source.common.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Platform-neutral page model.
 *
 * Unlike the Android extension `Page`, this has no `android.net.Uri` field and does not implement
 * the Android-only `ProgressListener`.
 */
@Serializable
open class Page(
    val index: Int,
    var url: String = "",
    var imageUrl: String? = null,
) {

    val number: Int
        get() = index + 1

    @Transient
    private val _statusFlow = MutableStateFlow<State>(State.Queue)

    @Transient
    val statusFlow = _statusFlow.asStateFlow()

    var status: State
        get() = _statusFlow.value
        set(value) {
            _statusFlow.value = value
        }

    @Transient
    private val _progressFlow = MutableStateFlow(0)

    @Transient
    val progressFlow = _progressFlow.asStateFlow()

    var progress: Int
        get() = _progressFlow.value
        set(value) {
            _progressFlow.value = value
        }

    fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        progress = if (contentLength > 0) {
            (100 * bytesRead / contentLength).toInt()
        } else {
            -1
        }
    }

    sealed interface State {
        data object Queue : State
        data object LoadPage : State
        data object DownloadImage : State
        data object Ready : State
        data class Error(val error: Throwable) : State
    }
}
// KMK <--
