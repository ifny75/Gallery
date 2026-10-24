package com.dot.gallery.core

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.gallery.core.util.MetadataCopyExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BatchMetadataState(
    val running: Boolean = false, val total: Int = 0,
    val processed: Int = 0, val saved: Int = 0, val finished: Boolean = false,
)

@HiltViewModel
class BatchMetadataViewModel @Inject constructor(private val exporter: MetadataCopyExporter) : ViewModel() {
    private val _state = MutableStateFlow(BatchMetadataState())
    val state = _state.asStateFlow()

    fun export(uris: List<Uri>) {
        if (_state.value.running || uris.isEmpty()) return
        val snapshot = uris.distinct()
        _state.value = BatchMetadataState(running = true, total = snapshot.size)
        viewModelScope.launch(Dispatchers.IO) {
            var saved = 0
            try {
                snapshot.forEachIndexed { index, uri ->
                    try {
                        if (exporter.export(uri) != null) saved++
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Report partial success; do not abort the remaining photographs.
                    } catch (_: OutOfMemoryError) {
                        // A huge source can exceed memory; each item is released before the next.
                    }
                    _state.value = BatchMetadataState(true, snapshot.size, index + 1, saved)
                }
            } finally {
                _state.value = _state.value.copy(running = false, saved = saved, finished = true)
            }
        }
    }

    fun acknowledge() { if (!_state.value.running) _state.value = BatchMetadataState() }
}
