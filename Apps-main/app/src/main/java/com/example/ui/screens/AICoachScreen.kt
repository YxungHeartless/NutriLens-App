package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Coach") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Instant Suggestion Engine Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { SuggestionChip(onClick = {}, label = { Text("Suggest High Protein Meal") }) }
                item { SuggestionChip(onClick = {}, label = { Text("Analyze my macros") }) }
                item { SuggestionChip(onClick = {}, label = { Text("Low carb alternatives") }) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blank chat history column
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Chat history placeholder...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Empty text input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask your AI Coach...") },
                    enabled = false // Stub
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { /* TODO */ }, enabled = false) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
