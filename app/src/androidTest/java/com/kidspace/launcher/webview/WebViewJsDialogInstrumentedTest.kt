package com.kidspace.launcher.webview

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class WebViewJsDialogInstrumentedTest {

    @Test
    fun confirmDialog_isVisibleWhenHostReady() {
        WebViewDialogHarnessActivity.launch().use { scenario ->
            scenario.onActivity { activity ->
                activity.showConfirm(CONFIRM_MESSAGE)
            }
            onView(withText(CONFIRM_MESSAGE)).check(matches(isDisplayed()))
            onView(withText(android.R.string.ok)).perform(click())
            scenario.onActivity { activity ->
                verify(activity.lastJsResult!!).confirm()
            }
        }
    }

    @Test
    fun confirmDialog_appearsAfterSimulatedFilePickerResumeSequence() {
        WebViewDialogHarnessActivity.launch().use { scenario ->
            scenario.onActivity { activity ->
                activity.simulateFilePickerCallbackThenConfirm(CONFIRM_MESSAGE)
            }
            onView(withText(CONFIRM_MESSAGE)).check(matches(isDisplayed()))
            onView(withText(android.R.string.ok)).perform(click())
            scenario.onActivity { activity ->
                verify(activity.lastJsResult!!).confirm()
            }
        }
    }

    private companion object {
        const val CONFIRM_MESSAGE =
            "Alle lokalen App-Daten auf diesem Gerät durch die Backup-Datei ersetzen?"
    }
}
