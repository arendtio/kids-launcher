package com.kidspace.launcher.webview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WebViewHostResumeGateTest {
    private lateinit var gate: WebViewHostResumeGate
    private var hostValid = true

    @Before
    fun setUp() {
        hostValid = true
        gate = WebViewHostResumeGate { hostValid }
    }

    @Test
    fun runWhenReady_executesImmediatelyWhenReady() {
        gate.markReady()
        var executed = false
        gate.runWhenReady { executed = true }
        assertTrue(executed)
        assertFalse(gate.hasPendingActions)
    }

    @Test
    fun runWhenReady_queuesUntilMarkReady() {
        var executed = false
        gate.runWhenReady { executed = true }
        assertFalse(executed)
        assertEquals(1, gate.pendingActionCount)
        gate.markReady()
        assertTrue(executed)
        assertFalse(gate.hasPendingActions)
    }

    @Test
    fun markNotReady_blocksImmediateExecution() {
        gate.markReady()
        gate.markNotReady()
        var executed = false
        gate.runWhenReady { executed = true }
        assertFalse(executed)
        assertEquals(1, gate.pendingActionCount)
    }

    @Test
    fun markReady_flushesMultiplePendingActionsInOrder() {
        val order = mutableListOf<Int>()
        gate.runWhenReady { order.add(1) }
        gate.runWhenReady { order.add(2) }
        gate.runWhenReady { order.add(3) }
        gate.markReady()
        assertEquals(listOf(1, 2, 3), order)
    }

    @Test
    fun clearPending_dropsQueuedActions() {
        gate.runWhenReady { error("should not run") }
        gate.clearPending()
        gate.markReady()
        assertFalse(gate.hasPendingActions)
    }

    @Test
    fun runWhenReady_doesNotExecuteWhenHostInvalid() {
        gate.markReady()
        hostValid = false
        var executed = false
        gate.runWhenReady { executed = true }
        assertFalse(executed)
        assertEquals(1, gate.pendingActionCount)
    }

    @Test
    fun markReady_doesNotFlushWhenHostInvalid() {
        var executed = false
        gate.runWhenReady { executed = true }
        hostValid = false
        gate.markReady()
        assertFalse(executed)
        assertEquals(1, gate.pendingActionCount)
    }

    @Test
    fun filePickerScenario_callbackBeforeWebViewResumeThenConfirm() {
        val events = mutableListOf<String>()
        gate.markReady()
        gate.markNotReady()

        gate.runWhenReady {
            events.add("file-delivered")
            events.add("confirm-requested")
        }

        assertEquals(emptyList<String>(), events)
        gate.markReady()
        assertEquals(listOf("file-delivered", "confirm-requested"), events)
    }
}
