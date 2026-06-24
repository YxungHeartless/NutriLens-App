package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Scanner", "Manual")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("Log") })
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTabIndex == 0) {
                // Scanner Tab
                ScannerTab(hasCameraPermission, permissionLauncher)
            } else {
                // Manual Tab
                ManualTab()
            }
        }
    }
}

@Composable
fun ScannerTab(
    hasCameraPermission: Boolean,
    permissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>
) {
    var scannedBarcode by remember { mutableStateOf("") }
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            RadarCameraPreview(
                isFlashOn = isFlashOn,
                onBarcodeScanned = { scannedBarcode = it }
            )

            // Overlays: Target reticles
            Text(
                text = "[   ]",
                color = Color.Green,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top Status Bar (e.g. barcode overlay view state)
            if (scannedBarcode.isNotEmpty()) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Scanned: $scannedBarcode",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Action Buttons Overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.extraLarge)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Toggle Flash", tint = if (isFlashOn) Color.Yellow else Color.White)
                }
                
                Button(
                    onClick = { /* Auto-Capture Logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Auto-Capture")
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Camera Access Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }
}

@Composable
fun ManualTab() {
    var searchQuery by remember { mutableStateOf("") }
    
    val allFrequentEntries = listOf(
        "Oatmeal", "Protein Shake", "Chicken Breast", "Brown Rice", 
        "Greek Yogurt", "Almonds", "Eggs", "Banana"
    )
    
    val filteredEntries = if (searchQuery.isBlank()) {
        allFrequentEntries
    } else {
        allFrequentEntries.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Predictive Text Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type food name...") },
                label = { Text("Search Foods Database") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
        }
        
        // Recent / Frequent Entries (Predictive Results)
        if (filteredEntries.isNotEmpty()) {
            item {
                Text("Recent / Frequent Entries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(filteredEntries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = entry,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            item {
                Text("No results found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Manual Preset Form
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Manual Quick Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Calories") }, modifier = Modifier.weight(1f), enabled = false)
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Protein (g)") }, modifier = Modifier.weight(1f), enabled = false)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Carbs (g)") }, modifier = Modifier.weight(1f), enabled = false)
                        OutlinedTextField(value = "", onValueChange = {}, label = { Text("Fats (g)") }, modifier = Modifier.weight(1f), enabled = false)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO */ }, modifier = Modifier.align(Alignment.End)) {
                        Text("Save Preset")
                    }
                }
            }
        }

        // Custom Recipe Builder
        item {
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Custom Recipe")
            }
        }

        // Today's Logs
        item {
            Text("Today's Logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item { MealCategoryStub("Breakfast") }
        item { MealCategoryStub("Lunch") }
        item { MealCategoryStub("Dinner") }
    }
}

@Composable
fun RadarCameraPreview(
    isFlashOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    LaunchedEffect(isFlashOn) {
        cameraController.enableTorch(isFlashOn)
    }

    LaunchedEffect(Unit) {
        val analyzer = BarcodeAnalyzer { barcode ->
            onBarcodeScanned(barcode)
        }
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(context),
            analyzer
        )
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_CENTER
                controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = Modifier.fillMaxSize(),
        onRelease = {
            cameraController.unbind()
        }
    )
}

@Composable
fun MealCategoryStub(mealName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(mealName, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No items logged.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
