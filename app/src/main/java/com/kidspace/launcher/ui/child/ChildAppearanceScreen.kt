package com.kidspace.launcher.ui.child

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidspace.launcher.data.model.AppearanceSettings
import com.kidspace.launcher.ui.components.AppearancePickerContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildAppearanceScreen(
    settings: AppearanceSettings,
    onSave: (AppearanceSettings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember(settings) { mutableStateOf(settings) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Look", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4F9AD8),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Text(
                    "Pick your favorite background and colors!",
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                AppearancePickerContent(
                    draft = draft,
                    onDraftChange = { draft = it },
                    showPhotoSection = false,
                )
                Button(
                    onClick = {
                        onSave(draft)
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Text("Save My Look")
                }
            }
        }
    }
}
