package com.example.docscanner.data

import kotlinx.coroutines.flow.MutableStateFlow

/** Returns only the result computed for the state that actually won the CAS. */
internal inline fun <T, R> atomicStateUpdate(
    state: MutableStateFlow<T>,
    transform: (T) -> Pair<T, R>
): R {
    while (true) {
        val before = state.value
        val (after, result) = transform(before)
        if (state.compareAndSet(before, after)) return result
    }
}
