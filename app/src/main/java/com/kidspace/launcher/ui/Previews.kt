package com.kidspace.launcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.data.model.ChildTile
import com.kidspace.launcher.data.model.TileType
import com.kidspace.launcher.domain.ParentGateChallenge
import com.kidspace.launcher.ui.child.ChildHomeScreen
import com.kidspace.launcher.ui.parent.ParentGateScreen

@Preview(showBackground = true, widthDp = 400, heightDp = 800, name = "Child home — portrait")
@Composable
private fun PreviewChildHome() {
    ChildHomeScreen(
        tiles = listOf(
            ChildTile(1, TileType.YOUTUBE, "Peppa Pig", "https://youtube.com/watch?v=example1", "youtube:example1", 0),
            ChildTile(2, TileType.APP, "Khan Academy", "org.khanacademy.android", "app:org.khanacademy.android", 1),
            ChildTile(3, TileType.WEBSITE, "PBS Kids", "https://pbskids.org", "random:star", 2),
            ChildTile(4, TileType.APP, "Duolingo", "com.duolingo", "app:com.duolingo", 3),
            ChildTile(5, TileType.WEBSITE, "Stories", "https://storybooks.app", "random:heart", 4),
        ),
        appearance = AppearanceSettings(),
        onTileClick = {},
        onAppearanceTileClick = {},
        onParentAccessRequest = {},
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400, name = "Child home — landscape")
@Composable
private fun PreviewChildHomeLandscape() {
    ChildHomeScreen(
        tiles = listOf(
            ChildTile(1, TileType.YOUTUBE, "Peppa Pig", "https://youtube.com/watch?v=example1", "youtube:example1", 0),
            ChildTile(2, TileType.APP, "Khan Academy", "org.khanacademy.android", "app:org.khanacademy.android", 1),
            ChildTile(3, TileType.WEBSITE, "PBS Kids", "https://pbskids.org", "random:star", 2),
            ChildTile(4, TileType.APP, "Duolingo", "com.duolingo", "app:com.duolingo", 3),
        ),
        appearance = AppearanceSettings(),
        onTileClick = {},
        onAppearanceTileClick = {},
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
        onCancel = {},
    )
}
