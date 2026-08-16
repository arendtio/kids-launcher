package com.kidspace.launcher.shortcuts

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Notifies the launcher when home-screen shortcuts change (pin/uninstall/legacy install). */
object ShortcutRefreshBus {
    private val _requests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val requests = _requests.asSharedFlow()

    fun requestRefresh() {
        _requests.tryEmit(Unit)
    }
}
