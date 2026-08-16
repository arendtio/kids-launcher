package com.kidspace.launcher.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidspace.launcher.update.AppUpdateConfig

@Composable
fun SystemTab(
    tileCount: Int,
    statusMessage: String?,
    statusIsError: Boolean,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onDismissStatus: () -> Unit,
    appVersionName: String,
    isUpdateDownloading: Boolean,
    updateStatusMessage: String?,
    updateStatusIsError: Boolean,
    canInstallUpdate: Boolean,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onDismissUpdateStatus: () -> Unit,
    webViewUploadDebugEnabled: Boolean,
    onWebViewUploadDebugChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) onExport(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    Column(modifier = modifier.fillMaxSize()) {
        SystemStatusBanner(
            backupMessage = statusMessage,
            backupIsError = statusIsError,
            onDismissBackup = onDismissStatus,
            updateMessage = updateStatusMessage,
            updateIsError = updateStatusIsError,
            onDismissUpdate = onDismissUpdateStatus,
            canInstallUpdate = canInstallUpdate,
            isUpdateDownloading = isUpdateDownloading,
            onInstallUpdate = onInstallUpdate,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SystemSectionHeader(
                    title = "Backup & Restore",
                    description = "Export or import tiles and appearance settings as a JSON file.",
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("What's included", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• $tileCount child tiles (apps, websites, YouTube links)", fontSize = 14.sp)
                        Text("• Background preset or custom photo", fontSize = 14.sp)
                        Text("• Color theme settings", fontSize = 14.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { exportLauncher.launch("kidspace-backup.json") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Text("  Export")
                    }
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Text("  Import")
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                SystemSectionHeader(
                    title = "App Updates",
                    description = "Download the latest debug build from GitHub and install it on this device.",
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Installed version: $appVersionName",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "After installing an update, reopen KidSpace.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Source: ${AppUpdateConfig.DEBUG_APK_URL}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onDownloadUpdate,
                    enabled = !isUpdateDownloading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isUpdateDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Text("  Downloading…")
                    } else {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Text("  Download latest version")
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }

            item {
                SystemSectionHeader(
                    title = "Advanced",
                    description = "Developer options for troubleshooting the in-app browser.",
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = null,
                            tint = Color(0xFF3949AB),
                            modifier = Modifier.padding(end = 12.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WebView upload debug console", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Shows an on-screen log during file uploads in the in-app browser. Off by default.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                            )
                        }
                        Switch(
                            checked = webViewUploadDebugEnabled,
                            onCheckedChange = onWebViewUploadDebugChange,
                        )
                    }
                }
            }
        }
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Import backup?") },
            text = {
                Text(
                    "This will replace all current tiles and appearance settings with the contents of the backup file.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onImport(pendingImportUri!!)
                    pendingImportUri = null
                }) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SystemSectionHeader(
    title: String,
    description: String,
) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text(description, fontSize = 14.sp, color = Color.Gray)
}

@Composable
private fun SystemStatusBanner(
    backupMessage: String?,
    backupIsError: Boolean,
    onDismissBackup: () -> Unit,
    updateMessage: String?,
    updateIsError: Boolean,
    onDismissUpdate: () -> Unit,
    canInstallUpdate: Boolean,
    isUpdateDownloading: Boolean,
    onInstallUpdate: () -> Unit,
) {
    val hasContent = backupMessage != null ||
        updateMessage != null ||
        (canInstallUpdate && !isUpdateDownloading)

    if (!hasContent) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        backupMessage?.let { message ->
            StatusCard(
                message = message,
                isError = backupIsError,
                onDismiss = onDismissBackup,
            )
        }
        updateMessage?.let { message ->
            StatusCard(
                message = message,
                isError = updateIsError,
                onDismiss = onDismissUpdate,
                actionLabel = if (canInstallUpdate && !updateIsError) "Install update" else null,
                onAction = if (canInstallUpdate && !updateIsError) onInstallUpdate else null,
            )
        }
        if (canInstallUpdate && updateMessage == null && !isUpdateDownloading) {
            StatusCard(
                message = "Update downloaded. Tap Install update to open the installer.",
                isError = false,
                onDismiss = onDismissUpdate,
                actionLabel = "Install update",
                onAction = onInstallUpdate,
            )
        }
    }
}

@Composable
private fun StatusCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
        ),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = message,
                color = if (isError) Color(0xFFC62828) else Color(0xFF2E7D32),
                fontSize = 14.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (actionLabel != null && onAction != null) {
                    TextButton(onClick = onAction) {
                        Text(actionLabel)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}
