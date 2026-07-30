package com.kidspace.launcher.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackupTab(
    tileCount: Int,
    statusMessage: String?,
    statusIsError: Boolean,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
    onDismissStatus: () -> Unit,
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Export all tiles and appearance settings to a JSON file, or import a previous backup.",
                fontSize = 14.sp,
                color = Color.Gray,
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
            Button(
                onClick = { exportLauncher.launch("kidspace-backup.json") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Text("  Export to JSON file")
            }
        }

        item {
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.FileUpload, contentDescription = null)
                Text("  Import from JSON file")
            }
        }

        if (statusMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusIsError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = statusMessage,
                            color = if (statusIsError) Color(0xFFC62828) else Color(0xFF2E7D32),
                            fontSize = 14.sp,
                        )
                        TextButton(onClick = onDismissStatus) {
                            Text("Dismiss")
                        }
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
                Text("This will replace all current tiles and appearance settings with the contents of the backup file.")
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
