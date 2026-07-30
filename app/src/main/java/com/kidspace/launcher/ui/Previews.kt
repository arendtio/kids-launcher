package com.kidspace.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.domain.ParentGateChallenge
import com.kidspace.launcher.ui.child.ChildHomeScreen
import com.kidspace.launcher.ui.parent.ParentGateScreen

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun PreviewChildHome() {
    ChildHomeScreen(
        tiles = listOf(
            ChildTile(1, TileType.APP, "YouTube Kids", "com.google.android.apps.youtube.kids", "app:com.google.android.apps.youtube.kids", 0),
            ChildTile(2, TileType.WEBSITE, "PBS Kids", "https://pbskids.org", "random:star", 1),
        ),
        appearance = AppearanceSettings(),
        onTileClick = {},
        onParentAccessRequest = {},
    )
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun PreviewParentGate() {
    ParentGateScreen(
        challenge = ParentGateChallenge.Challenge("three, seven, one", "371"),
        enteredDigits = "37",
        errorMessage = null,
        onDigit = {},
        onBackspace = {},
        onSubmit = {},
        onCancel = {},
    )
}
