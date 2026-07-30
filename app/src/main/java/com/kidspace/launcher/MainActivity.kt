package com.kidspace.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidspace.launcher.domain.LauncherActions
import com.kidspace.launcher.ui.AppScreen
import com.kidspace.launcher.ui.LauncherViewModel
import com.kidspace.launcher.ui.child.ChildHomeScreen
import com.kidspace.launcher.ui.parent.ParentGateScreen
import com.kidspace.launcher.ui.parent.ParentModeScreen
import com.kidspace.launcher.ui.theme.toComposeColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as KidSpaceApplication

        setContent {
            val viewModel: LauncherViewModel = viewModel(
                factory = LauncherViewModel.Factory(
                    app.tileRepository,
                    app.appRepository,
                    app.appearanceRepository,
                ),
            )

            val screen by viewModel.screen.collectAsState()
            val tiles by viewModel.tiles.collectAsState()
            val appearance by viewModel.appearance.collectAsState()
            val parentGate by viewModel.parentGate.collectAsState()
            val installedApps by viewModel.installedApps.collectAsState()

            val colorScheme = lightColorScheme(
                primary = appearance.primaryColor.toComposeColor(),
                secondary = appearance.secondaryColor.toComposeColor(),
                tertiary = appearance.accentColor.toComposeColor(),
                background = Color(0xFFF5F5F5),
            )

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (screen) {
                        AppScreen.CHILD -> ChildHomeScreen(
                            tiles = tiles,
                            appearance = appearance,
                            onTileClick = { tile ->
                                LauncherActions.launchTile(this, tile.type, tile.target)
                            },
                            onParentAccessRequest = viewModel::requestParentAccess,
                        )
                        AppScreen.PARENT_GATE -> ParentGateScreen(
                            challenge = parentGate.challenge,
                            enteredDigits = parentGate.enteredDigits,
                            errorMessage = parentGate.errorMessage,
                            onDigit = viewModel::onParentDigit,
                            onBackspace = viewModel::onParentBackspace,
                            onSubmit = viewModel::submitParentGate,
                            onCancel = viewModel::cancelParentGate,
                        )
                        AppScreen.PARENT -> ParentModeScreen(
                            tiles = tiles,
                            installedApps = installedApps,
                            appearance = appearance,
                            onExitParentMode = viewModel::exitParentMode,
                            onAddApp = viewModel::addAppTile,
                            onAddLink = viewModel::addLinkTile,
                            onRemoveTile = viewModel::removeTile,
                            onMoveTile = viewModel::moveTile,
                            onSaveAppearance = viewModel::saveAppearance,
                        )
                    }
                }
            }
        }
    }
}
