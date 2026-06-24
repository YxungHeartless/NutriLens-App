package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraCharacteristics
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.viewmodel.AiScannerViewModel
import com.example.ui.viewmodel.AiScanState
import com.example.data.database.SubscriptionTier
import org.koin.androidx.compose.koinViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LogFoodScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    mealTypeString: String,
    aiScannerViewModel: AiScannerViewModel = koinViewModel()
) {
    val mealType = MealType.fromString(mealTypeString)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val localSoftLime = SoftLime
    val localSlateBg = SlateBg
    val localHealthyGreen = HealthyGreen
    val localGrayText = GrayText

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    val shouldShowRationale = (cameraPermissionState.status as? PermissionStatus.Denied)?.shouldShowRationale == true

    val analysisState by viewModel.cameraAnalysisState.collectAsState()
    val aiScanState by aiScannerViewModel.scanState.collectAsState()

    val subscriptionTier by viewModel.premiumManager.subscriptionTier.collectAsState()
    val hasUltraAccess = subscriptionTier == SubscriptionTier.ULTRA

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

    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualFoodName by remember { mutableStateOf("") }
    var manualCalories by remember { mutableStateOf("") }
    var manualProtein by remember { mutableStateOf("") }
    var manualCarbs by remember { mutableStateOf("") }
    var manualFats by remember { mutableStateOf("") }
    var manualPortionSize by remember { mutableStateOf("1.0") }

    val coroutineScope = rememberCoroutineScope()
    var isFetchingLocation by remember { mutableStateOf(false) }
    var fetchedLocationName by remember { mutableStateOf<String?>(null) }
    var fetchedLatitude by remember { mutableStateOf<Double?>(null) }
    var fetchedLongitude by remember { mutableStateOf<Double?>(null) }
    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

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
            // Reset location state for new item
            fetchedLocationName = null
            fetchedLatitude = null
            fetchedLongitude = null
        }
    }

    // Bind current details on AI visual scanner success
    LaunchedEffect(aiScanState) {
        val resState = aiScanState
        if (resState is AiScanState.Success) {
            val res = resState.result
            customFoodName = res.name
            customCalories = res.calories.toInt().toString()
            customProtein = res.protein.toInt().toString()
            customCarbs = res.carbs.toInt().toString()
            customFats = res.fat.toInt().toString()
            customServingSize = "1.0"
            customServingUnit = "serving"
            showEditSheet = true
            // Reset location state for new item
            fetchedLocationName = null
            fetchedLatitude = null
            fetchedLongitude = null
        }
    }

    // Probe camera hardware availability
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraIds = remember {
        try {
            cameraManager.cameraIdList.toList()
        } catch (e: Exception) {
            emptyList<String>()
        }
    }
    val hasCameraFeature = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    val resolvedCameraSelector = remember(cameraIds) {
        var hasBack = false
        var hasFront = false
        for (id in cameraIds) {
            try {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    hasBack = true
                } else if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    hasFront = true
                }
            } catch (e: Exception) {
                Log.e("CameraRouting", "Error reading characteristics for camera $id", e)
            }
        }
        when {
            hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
            hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> null
        }
    }

    var isCameraHardwareAvailable by remember {
        mutableStateOf(resolvedCameraSelector != null && hasCameraFeature && cameraIds.isNotEmpty())
    }

    // Set up standard CameraX controller
    val cameraController = remember(resolvedCameraSelector) {
        if (resolvedCameraSelector != null && isCameraHardwareAvailable) {
            try {
                LifecycleCameraController(context).apply {
                    setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
                    cameraSelector = resolvedCameraSelector
                }
            } catch (e: Exception) {
                Log.e("CameraX", "Failed to initialize LifecycleCameraController", e)
                isCameraHardwareAvailable = false
                null
            }
        } else {
            null
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
                if (!hasUltraAccess) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SlateBg)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Ultra Premium Feature",
                            tint = SoftLime,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI Fridge & Plate Scanner",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock Ultra Tier to capture plate photos, detect hidden variables like cooking oils or dressings, and calculate macros instantly.",
                            color = GrayText,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("paywall") },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Upgrade to Ultra", color = SlateBg, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showManualInputDialog = true },
                            border = BorderStroke(1.dp, SoftLime),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLime),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Enter Manually (Free)", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    if (isCameraHardwareAvailable && cameraController != null) {
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
                        // Glassmorphic Virtual Camera Simulator
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(localSlateBg),
                            contentAlignment = Alignment.Center
                        ) {
                            // Shimmering ambient aura background
                            var timeState by remember { mutableStateOf(0f) }
                            LaunchedEffect(Unit) {
                                val startTime = System.currentTimeMillis()
                                while (true) {
                                    timeState = (System.currentTimeMillis() - startTime) / 1000f
                                    kotlinx.coroutines.delay(16)
                                }
                            }
                            
                            // High-tech flowing visual wave representation of sensor field
                            GeminiFluidBackground(
                                time = timeState,
                                modifier = Modifier.fillMaxSize(),
                                isDark = true
                            )
                            
                            // Glassmorphic camera grid overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .geminiGlowBorder(borderWidth = 1.5.dp, cornerRadius = 24.dp)
                            ) {
                                // Scanning grid lines and focus brackets
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeColor = localSoftLime.copy(alpha = 0.25f)
                                    val crispColor = localSoftLime
                                    val sizeW = size.width
                                    val sizeH = size.height
                                    
                                    // Vertical Thirds
                                    drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(sizeW / 3f, 0f), end = androidx.compose.ui.geometry.Offset(sizeW / 3f, sizeH), strokeWidth = 1.dp.toPx())
                                    drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(sizeW * 2 / 3f, 0f), end = androidx.compose.ui.geometry.Offset(sizeW * 2 / 3f, sizeH), strokeWidth = 1.dp.toPx())
                                    
                                    // Horizontal Thirds
                                    drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(0f, sizeH / 3f), end = androidx.compose.ui.geometry.Offset(sizeW, sizeH / 3f), strokeWidth = 1.dp.toPx())
                                    drawLine(strokeColor, start = androidx.compose.ui.geometry.Offset(0f, sizeH * 2 / 3f), end = androidx.compose.ui.geometry.Offset(sizeW, sizeH * 2 / 3f), strokeWidth = 1.dp.toPx())
                                    
                                    // Central Focus brackets
                                    val center = androidx.compose.ui.geometry.Offset(sizeW / 2f, sizeH / 2f)
                                    val bracketLength = 24.dp.toPx()
                                    val bracketGap = 40.dp.toPx()
                                    val bWidth = 2.dp.toPx()
                                    
                                    // Top Left Bracket
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(-bracketGap, -bracketGap), end = center + androidx.compose.ui.geometry.Offset(-bracketGap + bracketLength, -bracketGap), strokeWidth = bWidth)
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(-bracketGap, -bracketGap), end = center + androidx.compose.ui.geometry.Offset(-bracketGap, -bracketGap + bracketLength), strokeWidth = bWidth)
                                    
                                    // Top Right Bracket
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(bracketGap, -bracketGap), end = center + androidx.compose.ui.geometry.Offset(bracketGap - bracketLength, -bracketGap), strokeWidth = bWidth)
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(bracketGap, -bracketGap), end = center + androidx.compose.ui.geometry.Offset(bracketGap, -bracketGap + bracketLength), strokeWidth = bWidth)
                                    
                                    // Bottom Left Bracket
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(-bracketGap, bracketGap), end = center + androidx.compose.ui.geometry.Offset(-bracketGap + bracketLength, bracketGap), strokeWidth = bWidth)
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(-bracketGap, bracketGap), end = center + androidx.compose.ui.geometry.Offset(-bracketGap, bracketGap - bracketLength), strokeWidth = bWidth)
                                    
                                    // Bottom Right Bracket
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(bracketGap, bracketGap), end = center + androidx.compose.ui.geometry.Offset(bracketGap - bracketLength, bracketGap), strokeWidth = bWidth)
                                    drawLine(crispColor, start = center + androidx.compose.ui.geometry.Offset(bracketGap, bracketGap), end = center + androidx.compose.ui.geometry.Offset(bracketGap, bracketGap - bracketLength), strokeWidth = bWidth)
                                }
                                
                                // Moving laser scanning line
                                val infiniteTransition = rememberInfiniteTransition(label = "scanning_laser")
                                val scanProgress by infiniteTransition.animateFloat(
                                    initialValue = 0.05f,
                                    targetValue = 0.95f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(durationMillis = 3000, easing = EaseInOutSine),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "laser_y"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.015f)
                                        .align { sizeParent, sizeChild, _ ->
                                            androidx.compose.ui.unit.IntOffset(0, (sizeParent.height * scanProgress).toInt())
                                        }
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    localSoftLime.copy(alpha = 0.7f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                                
                                // Floating status telemetry text
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("VIRTUAL SCANNER ACTIVE", color = localSoftLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("ISO: 100 | RAW | AUTO-WHITE-BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
                                    Text("TRACKER STATE: ALIGNED", color = localHealthyGreen, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Simulated Camera",
                                    tint = localSoftLime.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(28.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Camera Permission Missing Placeholder
                val isPermanentlyDenied = !cameraPermissionState.status.isGranted && 
                        !cameraPermissionState.status.shouldShowRationale && 
                        hasRequestedPermission

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
                    if (isPermanentlyDenied) {
                        Text(
                            text = "Camera permission has been permanently denied. Please enable it in system settings to use the camera scanner.",
                            color = Color.Red.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("SettingsDeepLink", "Failed to open settings", e)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLime)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = SlateBg)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open App Settings", color = SlateBg, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                hasRequestedPermission = true
                                cameraPermissionState.launchPermissionRequest()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLime)
                        ) {
                            Text("Permit Camera Use", color = SlateBg, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showManualInputDialog = true },
                        border = BorderStroke(1.5.dp, SoftLime),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLime)
                    ) {
                        Text("Enter Manually", fontWeight = FontWeight.Bold)
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
                            if (!hasUltraAccess) {
                                navController.navigate("paywall")
                            } else {
                                val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                                capturedBitmap = dummyBitmap
                                aiScannerViewModel.scanFoodImage(dummyBitmap)
                            }
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
                            if (!hasUltraAccess) {
                                navController.navigate("paywall")
                            } else {
                                val dummyBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                                capturedBitmap = dummyBitmap
                                aiScannerViewModel.scanFoodImage(dummyBitmap)
                            }
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
                        if (!hasUltraAccess) {
                            navController.navigate("paywall")
                        } else {
                            val b1 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            val b2 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            val b3 = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                            viewModel.clearBatchQueue()
                            viewModel.addToBatchQueue(listOf(b1, b2, b3))
                        }
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
            if (cameraPermissionState.status.isGranted && hasUltraAccess) {
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
                            if (isCameraHardwareAvailable && cameraController != null) {
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
                                                aiScannerViewModel.scanFoodImage(bitmap)
                                            }
                                        }

                                        override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                                            Log.e("CameraX", "Trigger capture failed", exception)
                                        }
                                    }
                                )
                            } else {
                                // Simulate picture capture on browser/emulators by generating a mock bitmap
                                val dummyBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                                capturedBitmap = dummyBitmap
                                aiScannerViewModel.scanFoodImage(dummyBitmap)
                            }
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
            val isScannerLoading = analysisState is CameraAnalysisState.Loading || aiScanState is AiScanState.Loading
            if (isScannerLoading) {
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
            val errorStateMsg = when {
                analysisState is CameraAnalysisState.Error -> (analysisState as CameraAnalysisState.Error).message
                aiScanState is AiScanState.Error -> (aiScanState as AiScanState.Error).message
                else -> null
            }
            if (errorStateMsg != null) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.resetCameraState()
                        aiScannerViewModel.resetState()
                    },
                    title = { Text("Analysis Error", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = { Text(errorStateMsg, color = GrayText) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetCameraState()
                                aiScannerViewModel.resetState()
                                navController.navigate("paywall")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (errorStateMsg.contains("scans") || errorStateMsg.contains("ULTRA")) SoftLime else Color.Transparent),
                            modifier = Modifier.testTag("error_go_premium_button")
                        ) {
                            Text(
                                text = if (errorStateMsg.contains("scans") || errorStateMsg.contains("ULTRA")) "Unlock Premium" else "OK",
                                color = if (errorStateMsg.contains("scans") || errorStateMsg.contains("ULTRA")) SlateBg else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        if (errorStateMsg.contains("scans") || errorStateMsg.contains("ULTRA")) {
                            TextButton(onClick = {
                                viewModel.resetCameraState()
                                aiScannerViewModel.resetState()
                            }) {
                                Text("Dismiss", color = Color.White)
                            }
                        }
                    },
                    containerColor = CardDark
                )
            }

            // Visual results bottom sheets confirmation
            if (showEditSheet) {
                val isSuccess = analysisState is CameraAnalysisState.Success || aiScanState is AiScanState.Success
                if (isSuccess) {
                    val descText = if (analysisState is CameraAnalysisState.Success) {
                        (analysisState as CameraAnalysisState.Success).result.description
                    } else if (aiScanState is AiScanState.Success) {
                        "Gemini estimated nutritional content based on visual analysis of portion volumes."
                    } else {
                        null
                    }
                    AlertDialog(
                        onDismissRequest = {
                            showEditSheet = false
                            viewModel.resetCameraState()
                            aiScannerViewModel.resetState()
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
                                descText?.let {
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

                                Spacer(modifier = Modifier.height(4.dp))
                                if (fetchedLocationName != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .geminiGlowBorder(borderWidth = 1.dp, cornerRadius = 12.dp)
                                            .background(CardDark, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SoftLime, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("📍 At: $fetchedLocationName", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                } else if (isFetchingLocation) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Detecting location...", color = SoftLime, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        GeminiWaves(modifier = Modifier.fillMaxWidth(), isThinking = true, heightDp = 20.dp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (locationPermissionState.allPermissionsGranted) {
                                                isFetchingLocation = true
                                                coroutineScope.launch {
                                                    val locationHelper = com.example.domain.location.LocationHelper(context)
                                                    val loc = locationHelper.getCurrentLocation()
                                                    if (loc != null) {
                                                        fetchedLatitude = loc.latitude
                                                        fetchedLongitude = loc.longitude
                                                        val name = locationHelper.getLocationName(loc.latitude, loc.longitude)
                                                        fetchedLocationName = name ?: "Unknown Location"
                                                    } else {
                                                        fetchedLocationName = "Location not found"
                                                    }
                                                    isFetchingLocation = false
                                                }
                                            } else {
                                                locationPermissionState.launchMultiplePermissionRequest()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                        border = BorderStroke(1.dp, SoftLime.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SoftLime, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tag Current Location", color = SoftLime)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val nameVal = customFoodName.ifEmpty {
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.foodName
                                        else if (aiScanState is AiScanState.Success) (aiScanState as AiScanState.Success).result.name
                                        else ""
                                    }
                                    val caloriesVal = customCalories.toDoubleOrNull() ?: (
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.calories
                                        else if (aiScanState is AiScanState.Success) (aiScanState as AiScanState.Success).result.calories
                                        else 0.0
                                    )
                                    val proteinVal = customProtein.toDoubleOrNull() ?: (
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.protein
                                        else if (aiScanState is AiScanState.Success) (aiScanState as AiScanState.Success).result.protein
                                        else 0.0
                                    )
                                    val carbsVal = customCarbs.toDoubleOrNull() ?: (
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.carbs
                                        else if (aiScanState is AiScanState.Success) (aiScanState as AiScanState.Success).result.carbs
                                        else 0.0
                                    )
                                    val fatsVal = customFats.toDoubleOrNull() ?: (
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.fats
                                        else if (aiScanState is AiScanState.Success) (aiScanState as AiScanState.Success).result.fat
                                        else 0.0
                                    )
                                    val sizeVal = customServingSize.toDoubleOrNull() ?: (
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.servingSize
                                        else 1.0
                                    )
                                    val unitVal = customServingUnit.ifEmpty {
                                        if (analysisState is CameraAnalysisState.Success) (analysisState as CameraAnalysisState.Success).result.servingUnit
                                        else "serving"
                                    }

                                    viewModel.logFood(
                                        name = nameVal,
                                        calories = caloriesVal,
                                        protein = proteinVal,
                                        carbs = carbsVal,
                                        fats = fatsVal,
                                        mealType = mealType,
                                        servingSize = sizeVal,
                                        servingUnit = unitVal,
                                        latitude = fetchedLatitude,
                                        longitude = fetchedLongitude,
                                        locationName = fetchedLocationName
                                    )
                                    showEditSheet = false
                                    viewModel.resetCameraState()
                                    aiScannerViewModel.resetState()
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
                                aiScannerViewModel.resetState()
                            }) {
                                Text("Retry Capture", color = Color.White)
                            }
                        },
                        containerColor = CardDark
                    )
                }
            }

            if (showManualInputDialog) {
                AlertDialog(
                    onDismissRequest = { showManualInputDialog = false },
                    title = {
                        Text(
                            text = "Manual Food Entry",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = manualFoodName,
                                onValueChange = { manualFoodName = it },
                                label = { Text("Food Item Name", color = SoftLime) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftLime,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("manual_dialog_name_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = manualCalories,
                                    onValueChange = { manualCalories = it },
                                    label = { Text("Calories (kcal)", color = SoftLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftLime,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_dialog_calories_input")
                                )
                                OutlinedTextField(
                                    value = manualProtein,
                                    onValueChange = { manualProtein = it },
                                    label = { Text("Protein (g)", color = SoftLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftLime,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_dialog_protein_input")
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = manualCarbs,
                                    onValueChange = { manualCarbs = it },
                                    label = { Text("Carbs (g)", color = SoftLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftLime,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_dialog_carbs_input")
                                )
                                OutlinedTextField(
                                    value = manualFats,
                                    onValueChange = { manualFats = it },
                                    label = { Text("Fats (g)", color = SoftLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SoftLime,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("manual_dialog_fats_input")
                                )
                            }

                            OutlinedTextField(
                                value = manualPortionSize,
                                onValueChange = { manualPortionSize = it },
                                label = { Text("Portion Size (servings)", color = SoftLime) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SoftLime,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("manual_dialog_portion_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (manualFoodName.isNotBlank() && manualCalories.isNotBlank()) {
                                    viewModel.logFood(
                                        name = manualFoodName,
                                        calories = manualCalories.toDoubleOrNull() ?: 0.0,
                                        protein = manualProtein.toDoubleOrNull() ?: 0.0,
                                        carbs = manualCarbs.toDoubleOrNull() ?: 0.0,
                                        fats = manualFats.toDoubleOrNull() ?: 0.0,
                                        mealType = mealType,
                                        servingSize = manualPortionSize.toDoubleOrNull() ?: 1.0,
                                        servingUnit = "serving",
                                        latitude = fetchedLatitude,
                                        longitude = fetchedLongitude,
                                        locationName = fetchedLocationName
                                    )
                                    showManualInputDialog = false
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = false }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                            enabled = manualFoodName.isNotBlank() && manualCalories.isNotBlank()
                        ) {
                            Text("Log Food", color = SlateBg, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showManualInputDialog = false }) {
                            Text("Cancel", color = Color.White)
                        }
                    },
                    containerColor = CardDark
                )
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
