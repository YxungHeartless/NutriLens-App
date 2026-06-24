package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen() {
    var searchRadius by remember { mutableStateOf(5f) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Radar Discovery") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Search Bar and Map Stub
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search healthy options nearby...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Map Stub", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Map Feed Placeholder", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radius Slider
            Text("Search Radius: ${searchRadius.toInt()} km", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = searchRadius,
                onValueChange = { searchRadius = it },
                valueRange = 1f..20f,
                steps = 19
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("High-Protein Dining", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Results List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { RadarResultStub("Green Bowl Cafe", "3.2 km", "Keto, Vegan") }
                item { RadarResultStub("Protein Grill", "4.5 km", "High-Protein, Paleo") }
                item { RadarResultStub("Fit Kitchen", "1.1 km", "Balanced Macros") }
            }
        }
    }
}

@Composable
fun RadarResultStub(name: String, distance: String, tags: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(tags, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(distance, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}
