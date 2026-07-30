package com.kidspace.launcher.ui.parent

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kidspace.launcher.youtube.YouTubeSearchResult

@Composable
fun YouTubeSearchTab(
    apiKey: String,
    query: String,
    isSearching: Boolean,
    results: List<YouTubeSearchResult>,
    selectedVideoIds: Set<String>,
    childYouTubeVideoIds: Set<String>,
    errorMessage: String?,
    statusMessage: String?,
    onApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onAddSelected: () -> Unit,
    onDismissStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("YouTube Search", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Search YouTube, select videos, and add them to the child screen in one go.",
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
                    Text("YouTube API key", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create a free key in Google Cloud with YouTube Data API v3 enabled.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = onApiKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API key") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onSaveApiKey, modifier = Modifier.fillMaxWidth()) {
                        Text("Save API key")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Search term") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSearch,
                    enabled = !isSearching && query.isNotBlank(),
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${selectedVideoIds.size} selected", fontWeight = FontWeight.Medium)
                    Row {
                        TextButton(onClick = onSelectAll) { Text("Select all") }
                        TextButton(onClick = onClearSelection) { Text("Clear") }
                    }
                }
            }

            item {
                Button(
                    onClick = onAddSelected,
                    enabled = selectedVideoIds.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add ${selectedVideoIds.size} selected to child screen")
                }
            }
        }

        items(results, key = { it.videoId }) { result ->
            val alreadyAdded = result.videoId in childYouTubeVideoIds
            val isSelected = result.videoId in selectedVideoIds
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        alreadyAdded -> Color(0xFFE8F5E9)
                        isSelected -> Color(0xFFE8EAF6)
                        else -> Color.White
                    },
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelection(result.videoId) },
                        enabled = !alreadyAdded,
                    )
                    AsyncImage(
                        model = result.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp, 45.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.title,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.durationLabel,
                            fontSize = 12.sp,
                            color = Color.Gray,
                        )
                        if (alreadyAdded) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Already on child screen", fontSize = 11.sp, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            item {
                StatusMessageCard(message = errorMessage, isError = true, onDismiss = onDismissStatus)
            }
        }

        if (statusMessage != null) {
            item {
                StatusMessageCard(message = statusMessage, isError = false, onDismiss = onDismissStatus)
            }
        }
    }
}

@Composable
private fun StatusMessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = if (isError) Color(0xFFC62828) else Color(0xFF2E7D32),
                fontSize = 14.sp,
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}
