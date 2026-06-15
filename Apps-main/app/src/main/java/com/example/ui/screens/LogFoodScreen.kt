package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.data.api.FoodAnalysisResult
import com.example.domain.model.MealType
import com.example.ui.components.*
import com.example.ui.viewmodel.CameraAnalysisState
import com.example.ui.viewmodel.NutritionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LogFoodScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    mealTypeString: String
) {
    val mealType = MealType.fromString(mealTypeString)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    val analysisState by viewModel.cameraAnalysisState.collectAsState()

    // Holds capture thumbnail preview locally
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Interactive custom adjustments overlay states
    var showEditSheet by remember { mutableStateOf(false) }
    var customFoodName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var customProtein by remember { mutableStateOf("") }
    var customCarbs by remember { mutableStateOf("") }
    var customFats by remember { mutableStateOf("") }
    var customServingSize by remember { mutableStateOf("") }
    var customServingUnit by remember { mutableStateOf("") }

    // Bind current details on visual recognition success
    LaunchedEffect(analysisState) {
        val resState = analysisState
        if (resState is CameraAnalysisState.Success) {
            val res = resState.result
            customFoodName = res.foodName
            customCalories = res.calories.toInt().toString()
            customProtein = res.protein.toInt().toString()
            customCarbs = res.carbs.toInt().toString()
            customFats = res.fats.toInt().toString()
            customServingSize = res.servingSize.toString()
            customServingUnit = res.servingUnit
            showEditSheet = true
        }
    }

    // Set up standard CameraX controller
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Camera Log (${mealType.displayName})", color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Manual form path
                    IconButton(
                        onClick = { navController.navigate("manual_add/${mealType.name}") },
                        modifier = Modifier.testTag("nav_manual_add_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Manual entry form", tint = SoftLime)
                    }
                    // Barcode path
                    IconButton(
                        onClick = { navController.navigate("barcode_scanner/${mealType.name}") },
                        modifier = Modifier.testTag("nav_barcode_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Barcode scanning", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBg)
            )
        },
        containerColor = SlateBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Active Camera Feed View Finder
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            controller = cameraController
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Camera Permission Missing Placeholder
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .background(SlateBg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = GrayText.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera access is needed for visual tracking",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant camera privileges to analyze ingredients and estimate macro ratios in real time.",
                        color = GrayText,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftLime)
                    ) {
                        Text("Permit Camera Use", color = SlateBg, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick emulator shortcut panel (essential for visual logs on streaming emulation browser)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(14.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Streaming Browser Quick-Logs",
                    color = SoftLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No actual target camera available in browser sessions. Click any preset item below to trigger full visual analysis:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Mocking capture of Grilled Salmon Salad
                            val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            capturedBitmap = dummyBitmap
                            viewModel.analyzeFoodImage(dummyBitmap)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("quick_log_salmon")
                    ) {
                        Text("Salmon Salad 🌱", color = Color.White, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            // Mocking capture of Acai Bowl
                            val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                            capturedBitmap = dummyBitmap
                            viewModel.analyzeFoodImage(dummyBitmap)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("quick_log_acai")
                    ) {
                        Text("Acai Bowl 🫐", color = Color.White, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        // Create 3 dummy Bitmaps to run sequential background worker queue
                        val b1 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                        val b2 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                        val b3 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                        viewModel.clearBatchQueue()
                        viewModel.addToBatchQueue(listOf(b1, b2, b3))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, SoftLime.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("quick_log_batch_sim")
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = SoftLime, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ AI Batch Scan (3 Photos in Queue)", color = SoftLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Batch Queue list live rendering module
                val batchQueue by viewModel.batchQueue.collectAsState()
                if (batchQueue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Batch Processing Queue (${batchQueue.count { it.status == com.example.ui.viewmodel.BatchItemStatus.SUCCESS }} / ${batchQueue.size} Done)",
                            color = SoftLime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        IconButton(onClick = { viewModel.clearBatchQueue() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Queue", tint = Color.Red, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        batchQueue.forEachIndexed { index, item ->
                            val statusText = when (item.status) {
                                com.example.ui.viewmodel.BatchItemStatus.PENDING -> "⏳ Pending in queue background..."
                                com.example.ui.viewmodel.BatchItemStatus.PROCESSING -> "⚡ Parsing density sheen..."
                                com.example.ui.viewmodel.BatchItemStatus.SUCCESS -> "✅ ${item.result?.foodName} (${item.result?.calories?.toInt()} kcal)"
                                com.example.ui.viewmodel.BatchItemStatus.ERROR -> "❌ Error: ${item.error}"
                            }
                            val statusColor = when (item.status) {
                                com.example.ui.viewmodel.BatchItemStatus.PENDING -> Color.White.copy(alpha = 0.5f)
                                com.example.ui.viewmodel.BatchItemStatus.PROCESSING -> SoftLime
                                com.example.ui.viewmodel.BatchItemStatus.SUCCESS -> HealthyGreen
                                com.example.ui.viewmodel.BatchItemStatus.ERROR -> Color.Red
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateBg.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.status == com.example.ui.viewmodel.BatchItemStatus.PROCESSING) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = SoftLime)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Captured Plate #${index + 1}", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                                    Text(text = statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    item.result?.description?.let { desc ->
                                        Text(text = desc, color = GrayText, fontSize = 9.sp, maxLines = 1)
                                    }
                                }
                                if (item.status == com.example.ui.viewmodel.BatchItemStatus.SUCCESS && item.result != null) {
                                    Button(
                                        onClick = {
                                            viewModel.logFood(
                                                name = item.result.foodName,
                                                calories = item.result.calories,
                                                protein = item.result.protein,
                                                carbs = item.result.carbs,
                                                fats = item.result.fats,
                                                mealType = mealType,
                                                servingSize = item.result.servingSize,
                                                servingUnit = item.result.servingUnit
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthyGreen),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text("Log", color = SlateBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Bulk Approval logging action
                    val successItems = batchQueue.filter { it.status == com.example.ui.viewmodel.BatchItemStatus.SUCCESS }
                    if (successItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                successItems.forEach { item ->
                                    val res = item.result ?: return@forEach
                                    viewModel.logFood(
                                        name = res.foodName,
                                        calories = res.calories,
                                        protein = res.protein,
                                        carbs = res.carbs,
                                        fats = res.fats,
                                        mealType = mealType,
                                        servingSize = res.servingSize,
                                        servingUnit = res.servingUnit
                                    )
                                }
                                viewModel.clearBatchQueue()
                                navController.navigate("dashboard") {
                                    popUpTo("dashboard") { inclusive = false }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Text("Log All ${successItems.size} Approved Meals", color = SlateBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom camera layout actions
            if (cameraPermissionState.status.isGranted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(24.dp)
                ) {
                    // Shutter button
                    IconButton(
                        onClick = {
                            // Execute standard CameraX image capture and process resulting bitmap
                            val mainExecutor = ContextCompat.getMainExecutor(context)
                            cameraController.takePicture(
                                mainExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(image)
                                        image.close()
                                        if (bitmap != null) {
                                            capturedBitmap = bitmap
                                            viewModel.analyzeFoodImage(bitmap)
                                        }
                                    }

                                    override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                                        Log.e("CameraX", "Trigger capture failed", exception)
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center)
                            .background(Color.White, CircleShape)
                            .testTag("shutter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photo",
                            tint = SlateBg,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Processing and Loading Scanner Overlay
            if (analysisState is CameraAnalysisState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .width(300.dp)
                            .padding(16.dp)
                            .geminiGlowBorder(borderWidth = 1.5.dp, cornerRadius = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Analyzing",
                                tint = SoftLime,
                                modifier = Modifier.size(36.dp)
                            )
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "AI Plate Scanning...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "NutriLens deep neural networks are estimating macronutrient densities and food volumes...",
                                    color = GrayText,
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // High-tech Gemini waves flowing actively
                            GeminiWaves(
                                modifier = Modifier.fillMaxWidth(),
                                isThinking = true,
                                heightDp = 40.dp
                            )
                        }
                    }
                }
            }

            // Error Overlay Dialog
            if (analysisState is CameraAnalysisState.Error) {
                val errorMsg = (analysisState as CameraAnalysisState.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.resetCameraState() },
                    title = { Text("Analysis Error", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = { Text(errorMsg, color = GrayText) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetCameraState()
                                navController.navigate("paywall")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (errorMsg.contains("scans")) SoftLime else Color.Transparent),
                            modifier = Modifier.testTag("error_go_premium_button")
                        ) {
                            Text(
                                text = if (errorMsg.contains("scans")) "Unlock Premium" else "OK",
                                color = if (errorMsg.contains("scans")) SlateBg else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        if (errorMsg.contains("scans")) {
                            TextButton(onClick = { viewModel.resetCameraState() }) {
                                Text("Dismiss", color = Color.White)
                            }
                        }
                    },
                    containerColor = CardDark
                )
            }

            // Visual results bottom sheets confirmation
            if (showEditSheet) {
                val state = analysisState
                if (state is CameraAnalysisState.Success) {
                    AlertDialog(
                        onDismissRequest = {
                            showEditSheet = false
                            viewModel.resetCameraState()
                        },
                        title = {
                            Column {
                                Text(
                                    text = "AI Nutrient Estimation",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                state.result.description?.let {
                                    Text(
                                        text = it,
                                        color = GrayText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .verticalScroll(rememberScrollState())
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = customFoodName,
                                    onValueChange = { customFoodName = it },
                                    label = { Text("Food Item Name", color = SoftLime) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftLime,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("ai_food_name_input")
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = customCalories,
                                        onValueChange = { customCalories = it },
                                        label = { Text("Calories (kcal)", color = SoftLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_calories_input")
                                    )
                                    OutlinedTextField(
                                        value = customProtein,
                                        onValueChange = { customProtein = it },
                                        label = { Text("Protein (g)", color = SoftLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_protein_input")
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = customCarbs,
                                        onValueChange = { customCarbs = it },
                                        label = { Text("Carbohydrates (g)", color = SoftLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_carbs_input")
                                    )
                                    OutlinedTextField(
                                        value = customFats,
                                        onValueChange = { customFats = it },
                                        label = { Text("Fats (g)", color = SoftLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_fats_input")
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = customServingSize,
                                        onValueChange = { customServingSize = it },
                                        label = { Text("Serving Size", color = SoftLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_serving_size_input")
                                    )
                                    OutlinedTextField(
                                        value = customServingUnit,
                                        onValueChange = { customServingUnit = it },
                                        label = { Text("Serving Unit", color = SoftLime) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SoftLime,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f).testTag("ai_serving_unit_input")
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val nameVal = customFoodName.ifEmpty { state.result.foodName }
                                    val caloriesVal = customCalories.toDoubleOrNull() ?: state.result.calories
                                    val proteinVal = customProtein.toDoubleOrNull() ?: state.result.protein
                                    val carbsVal = customCarbs.toDoubleOrNull() ?: state.result.carbs
                                    val fatsVal = customFats.toDoubleOrNull() ?: state.result.fats
                                    val sizeVal = customServingSize.toDoubleOrNull() ?: state.result.servingSize
                                    val unitVal = customServingUnit.ifEmpty { state.result.servingUnit }

                                    viewModel.logFood(
                                        name = nameVal,
                                        calories = caloriesVal,
                                        protein = proteinVal,
                                        carbs = carbsVal,
                                        fats = fatsVal,
                                        mealType = mealType,
                                        servingSize = sizeVal,
                                        servingUnit = unitVal
                                    )
                                    showEditSheet = false
                                    viewModel.resetCameraState()
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = false }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                                modifier = Modifier.testTag("ai_log_confirm_button")
                            ) {
                                Text("Approve and Log", color = SlateBg, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showEditSheet = false
                                viewModel.resetCameraState()
                            }) {
                                Text("Retry Capture", color = Color.White)
                            }
                        },
                        containerColor = CardDark
                    )
                }
            }
        }
    }
}

// Helper: Converts image proxy planes to Bitmap
private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
