package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.window.Dialog
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.util.Calendar
import com.example.data.model.FoodEntry
import com.example.domain.model.MealType
import com.example.ui.components.*
import com.example.ui.viewmodel.NutritionViewModel
import kotlin.math.min
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.animation.ExperimentalSharedTransitionApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: NutritionViewModel,
    widthSizeClass: WindowWidthSizeClass,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val entries by viewModel.loggedMeals.collectAsState()
    val calorieGoal by viewModel.calorieGoal.collectAsState()
    val proteinGoal by viewModel.proteinGoal.collectAsState()
    val carbsGoal by viewModel.carbsGoal.collectAsState()
    val fatsGoal by viewModel.fatsGoal.collectAsState()

    val todayActiveCalories by viewModel.todayActiveCalories.collectAsState()
    var showGoalDialog by remember { mutableStateOf(false) }

    val allMeals by viewModel.allLoggedMeals.collectAsState()
    
    val weeklyCalories = remember(allMeals) {
        val list = mutableListOf<Float>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val dayEntries = allMeals.filter { it.timestamp in startOfDay..endOfDay }
            list.add(dayEntries.sumOf { it.calories }.toFloat())
        }
        list
    }

    val days = remember(allMeals) {
        val list = mutableListOf<String>()
        val formatter = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            list.add(formatter.format(cal.time))
        }
        list
    }

    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.protein }
    val totalCarbs = entries.sumOf { it.carbs }
    val totalFats = entries.sumOf { it.fats }

    var selectedEntryId by remember { mutableStateOf<Int?>(null) }
    val dashboardSections by viewModel.dashboardSections.collectAsState()
    var activeMacroFilter by remember { mutableStateOf("All") }
    var dashboardModeIndex by remember { mutableStateOf(0) } // 0 = Daily View, 1 = Weekly Trend

    val filteredEntries = remember(entries, activeMacroFilter) {
        when (activeMacroFilter) {
            "High Protein" -> entries.filter { it.protein >= 15.0 }
            "High Carbs" -> entries.filter { it.carbs >= 30.0 }
            "High Fats" -> entries.filter { it.fats >= 10.0 }
            else -> entries
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AURA NUTRITION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Personal Visual Tracker",
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("summary") },
                        modifier = Modifier.testTag("summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Weekly Summary Charts",
                            tint = SoftLime
                        )
                    }
                    IconButton(
                        onClick = { showGoalDialog = true },
                        modifier = Modifier.testTag("edit_goals_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Edit Macro Goals",
                            tint = SoftLime
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearDailyLogs() },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Logs",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateBg
                )
            )
        },
        floatingActionButton = {
            // Only show FABs on compact screen. Expanded screen has FAB in NavigationRail
            if (widthSizeClass == WindowWidthSizeClass.Compact) {
                var expanded by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box {
                        FloatingActionButton(
                            onClick = { expanded = !expanded },
                            containerColor = SoftLime,
                            modifier = Modifier.testTag("quick_log_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Quick Log Meal",
                                tint = SlateBg
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(CardDark)
                                .border(1.dp, SoftLime.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .testTag("quick_log_dropdown_menu")
                        ) {
                            DropdownMenuItem(
                                text = { Text("Breakfast", color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("log_food/${MealType.BREAKFAST.name}")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Restaurant, contentDescription = "Breakfast", tint = SoftLime, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.testTag("quick_log_item_breakfast")
                            )
                            DropdownMenuItem(
                                text = { Text("Lunch", color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("log_food/${MealType.LUNCH.name}")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Restaurant, contentDescription = "Lunch", tint = SoftLime, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.testTag("quick_log_item_lunch")
                            )
                            DropdownMenuItem(
                                text = { Text("Dinner", color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("log_food/${MealType.DINNER.name}")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Restaurant, contentDescription = "Dinner", tint = SoftLime, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.testTag("quick_log_item_dinner")
                            )
                            DropdownMenuItem(
                                text = { Text("Snack", color = Color.White, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    expanded = false
                                    navController.navigate("log_food/${MealType.SNACK.name}")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Restaurant, contentDescription = "Snack", tint = SoftLime, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.testTag("quick_log_item_snack")
                            )
                        }
                    }

                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate("ai_coach") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "AI Coach",
                                tint = SlateBg
                            )
                        },
                        text = { Text("AI Coach", color = SlateBg, fontWeight = FontWeight.Bold) },
                        containerColor = SoftLime,
                        modifier = Modifier.testTag("ai_coach_fab")
                    )
                }
            }
        },
        containerColor = SlateBg
    ) { paddingValues ->
        if (widthSizeClass == WindowWidthSizeClass.Compact) {
            // ==========================================
            // COMPACT PHONE PORTRAIT LAYOUT
            // ==========================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Segmented Button for View Toggle
                item {
                    val options = listOf("Daily Gauges", "Weekly Trend")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        options.forEachIndexed { idx, label ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = idx, count = options.size),
                                onClick = { dashboardModeIndex = idx },
                                selected = idx == dashboardModeIndex
                            ) {
                                Text(label, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Render Sections sequentially based on ordered sections preferences
                dashboardSections.forEach { sectionId ->
                    when (sectionId) {
                        "macro_gauges" -> {
                            if (dashboardModeIndex == 0) {
                                item(key = "macro_gauges") {
                                    ReorderableSectionWrapper(
                                        sectionId = "macro_gauges",
                                        title = "Macro Gauges",
                                        viewModel = viewModel,
                                        sectionOrder = dashboardSections
                                    ) {
                                        MacroCanvasDashboard(
                                            totalCalories = totalCalories,
                                            calorieGoal = calorieGoal,
                                            totalProtein = totalProtein,
                                            proteinGoal = proteinGoal,
                                            totalCarbs = totalCarbs,
                                            carbsGoal = carbsGoal,
                                            totalFats = totalFats,
                                            fatsGoal = fatsGoal
                                        )
                                    }
                                }
                            }
                        }
                        "weekly_summary" -> {
                            if (dashboardModeIndex == 1) {
                                item(key = "weekly_summary") {
                                    ReorderableSectionWrapper(
                                        sectionId = "weekly_summary",
                                        title = "Weekly Summary",
                                        viewModel = viewModel,
                                        sectionOrder = dashboardSections
                                    ) {
                                        WeeklySummarySection(
                                            weeklyCalories = weeklyCalories,
                                            calorieGoal = calorieGoal,
                                            days = days,
                                            todayActiveCalories = todayActiveCalories,
                                            onTrendsClick = { navController.navigate("summary") }
                                        )
                                    }
                                }
                            }
                        }
                        "companion_sync" -> {
                            item(key = "companion_sync") {
                                ReorderableSectionWrapper(
                                    sectionId = "companion_sync",
                                    title = "Companion Sync",
                                    viewModel = viewModel,
                                    sectionOrder = dashboardSections
                                ) {
                                    CompanionSyncSection(
                                        viewModel = viewModel,
                                        todayActiveCalories = todayActiveCalories
                                    )
                                }
                            }
                        }
                    }
                }

                // Meal shortcuts quick logger
                item {
                    MealShortcutsPanel(navController = navController)
                }

                // Logs list header with filter chips
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's logs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${filteredEntries.size} items",
                                fontSize = 12.sp,
                                color = GrayText
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("All", "High Protein", "High Carbs", "High Fats").forEach { filter ->
                                val selected = activeMacroFilter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { activeMacroFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // Logs items
                if (filteredEntries.isEmpty()) {
                    item {
                        EmptyLogsPlaceholder()
                    }
                } else {
                    itemsIndexed(filteredEntries, key = { _, entry -> entry.id }) { index, entry ->
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(entry.id) {
                            delay(index * 30L)
                            isVisible = true
                        }
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + slideInVertically(
                                initialOffsetY = { 24 },
                                animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                            ),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            FoodEntryRow(
                                entry = entry,
                                onDelete = { viewModel.deleteEntry(entry) },
                                onClick = { navController.navigate("food_detail/${entry.id}") },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // ==========================================
            // TABLET / FOLDABLE TWO-PANE LAYOUT
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Main Dashboard Controls & Lists
                LazyColumn(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        val options = listOf("Daily Gauges", "Weekly Trend")
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            options.forEachIndexed { idx, label ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(index = idx, count = options.size),
                                    onClick = { dashboardModeIndex = idx },
                                    selected = idx == dashboardModeIndex
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (dashboardModeIndex == 0) {
                        item(key = "macro_gauges_tablet") {
                            MacroCanvasDashboard(
                                totalCalories = totalCalories,
                                calorieGoal = calorieGoal,
                                totalProtein = totalProtein,
                                proteinGoal = proteinGoal,
                                totalCarbs = totalCarbs,
                                carbsGoal = carbsGoal,
                                totalFats = totalFats,
                                fatsGoal = fatsGoal
                            )
                        }
                    } else {
                        item(key = "weekly_summary_tablet") {
                            WeeklySummarySection(
                                weeklyCalories = weeklyCalories,
                                calorieGoal = calorieGoal,
                                days = days,
                                todayActiveCalories = todayActiveCalories,
                                onTrendsClick = { navController.navigate("summary") }
                            )
                        }
                    }

                    item {
                        CompanionSyncSection(
                            viewModel = viewModel,
                            todayActiveCalories = todayActiveCalories
                        )
                    }

                    item {
                        MealShortcutsPanel(navController = navController)
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Today's logs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${filteredEntries.size} items",
                                    fontSize = 12.sp,
                                    color = GrayText
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("All", "High Protein", "High Carbs", "High Fats").forEach { filter ->
                                    val selected = activeMacroFilter == filter
                                    FilterChip(
                                        selected = selected,
                                        onClick = { activeMacroFilter = filter },
                                        label = { Text(filter, fontSize = 11.sp) },
                                        leadingIcon = if (selected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }

                    if (filteredEntries.isEmpty()) {
                        item {
                            EmptyLogsPlaceholder()
                        }
                    } else {
                        itemsIndexed(filteredEntries, key = { _, entry -> entry.id }) { index, entry ->
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(entry.id) {
                                delay(index * 30L)
                                isVisible = true
                            }
                            AnimatedVisibility(
                                visible = isVisible,
                                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + slideInVertically(
                                    initialOffsetY = { 24 },
                                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                                ),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                FoodEntryRow(
                                    entry = entry,
                                    onDelete = {
                                        viewModel.deleteEntry(entry)
                                        if (selectedEntryId == entry.id) {
                                            selectedEntryId = null
                                        }
                                    },
                                    onClick = { selectedEntryId = entry.id },
                                    sharedTransitionScope = sharedTransitionScope,
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // Right Column: Embedded Detailed Panel (Two-Pane list-detail)
                Card(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    val selectedEntry = remember(filteredEntries, selectedEntryId) {
                        filteredEntries.find { it.id == selectedEntryId }
                    }

                    if (selectedEntry != null) {
                        FoodDetailContent(
                            entry = selectedEntry,
                            onDelete = {
                                viewModel.deleteEntry(selectedEntry)
                                selectedEntryId = null
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GrayText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Select a food log entry",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Click on any log on the left to see details.",
                                    color = GrayText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        val themeMode by viewModel.themeMode.collectAsState()
        val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()

        GoalEditorDialog(
            currentCal = calorieGoal,
            currentProt = proteinGoal,
            currentCarb = carbsGoal,
            currentFat = fatsGoal,
            currentThemeMode = themeMode,
            currentDynamicColor = dynamicColorEnabled,
            todayActiveCalories = todayActiveCalories,
            onDismiss = { showGoalDialog = false },
            onNavigateToPaywall = {
                showGoalDialog = false
                navController.navigate("paywall")
            },
            onSave = { cal, prot, carb, fat, theme, dynamic ->
                viewModel.updateGoals(cal, prot, carb, fat)
                viewModel.updateThemeMode(theme)
                viewModel.updateDynamicColorEnabled(dynamic)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun MacroCanvasDashboard(
    totalCalories: Double,
    calorieGoal: Double,
    totalProtein: Double,
    proteinGoal: Double,
    totalCarbs: Double,
    carbsGoal: Double,
    totalFats: Double,
    fatsGoal: Double
) {
    val animatedCalProgress by animateFloatAsState(
        targetValue = min(1.0, totalCalories / calorieGoal).toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val animatedProtProgress by animateFloatAsState(
        targetValue = min(1.0, totalProtein / proteinGoal).toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val animatedCarbProgress by animateFloatAsState(
        targetValue = min(1.0, totalCarbs / carbsGoal).toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    val animatedCalCount by animateFloatAsState(
        targetValue = totalCalories.toFloat(),
        animationSpec = tween(900, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("macros_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val hGreen = HealthyGreen
                val sLime = SoftLime
                val pOrange = ProteinOrange
                val cYellow = CarbYellow

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.horizontalGradient(listOf(hGreen, sLime)),
                        startAngle = -90f,
                        sweepAngle = animatedCalProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val pRadius = size.minDimension / 2 - strokeWidth - 6.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = center - androidx.compose.ui.geometry.Offset(pRadius, pRadius),
                        size = androidx.compose.ui.geometry.Size(pRadius * 2, pRadius * 2)
                    )
                    drawArc(
                        color = pOrange,
                        startAngle = -90f,
                        sweepAngle = animatedProtProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = center - androidx.compose.ui.geometry.Offset(pRadius, pRadius),
                        size = androidx.compose.ui.geometry.Size(pRadius * 2, pRadius * 2)
                    )

                    val cRadius = size.minDimension / 2 - (strokeWidth * 2) - 12.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = center - androidx.compose.ui.geometry.Offset(cRadius, cRadius),
                        size = androidx.compose.ui.geometry.Size(cRadius * 2, cRadius * 2)
                    )
                    drawArc(
                        color = cYellow,
                        startAngle = -90f,
                        sweepAngle = animatedCarbProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = center - androidx.compose.ui.geometry.Offset(cRadius, cRadius),
                        size = androidx.compose.ui.geometry.Size(cRadius * 2, cRadius * 2)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedCalCount.toInt()}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ ${calorieGoal.toInt()} kcal",
                        color = GrayText,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MacroIndicatorLine(
                    label = "Protein",
                    current = totalProtein,
                    goal = proteinGoal,
                    color = ProteinOrange,
                    unit = "g"
                )
                MacroIndicatorLine(
                    label = "Carbohydrates",
                    current = totalCarbs,
                    goal = carbsGoal,
                    color = CarbYellow,
                    unit = "g"
                )
                MacroIndicatorLine(
                    label = "Fats",
                    current = totalFats,
                    goal = fatsGoal,
                    color = FatBlue,
                    unit = "g"
                )
            }
        }
    }
}

@Composable
fun MacroIndicatorLine(
    label: String,
    current: Double,
    goal: Double,
    color: Color,
    unit: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = min(1.0, current / goal).toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val animatedValueCount by animateFloatAsState(
        targetValue = current.toFloat(),
        animationSpec = tween(900, easing = FastOutSlowInEasing)
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "${animatedValueCount.toInt()} / ${goal.toInt()} $unit",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = Color.White.copy(alpha = 0.05f)
        )
    }
}

@Composable
fun MealShortcutsPanel(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Select meal to log",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val meals = listOf(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER,
                MealType.SNACK
            )
            meals.forEach { meal ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("log_food/${meal.name}") }
                        .testTag("shortcut_${meal.name.lowercase()}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val icon = when (meal) {
                            MealType.BREAKFAST -> Icons.Default.WbSunny
                            MealType.LUNCH -> Icons.Default.Fastfood
                            MealType.DINNER -> Icons.Default.DinnerDining
                            MealType.SNACK -> Icons.Default.LocalCafe
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SoftLime.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = SoftLime, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = meal.displayName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FoodEntryRow(
    entry: FoodEntry,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(animatedScale)
                .sharedElement(
                    rememberSharedContentState(key = "food_card_${entry.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag("food_entry_row_${entry.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isPressed) CardDark.copy(alpha = 0.8f) else CardDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = if (isPressed) 0.08f else 0.03f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "food_name_${entry.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    Text(
                        text = "${entry.mealType.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} • ${entry.servingSize} ${entry.servingUnit}",
                        color = GrayText,
                        fontSize = 11.sp
                    )
                    if (entry.locationName != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SoftLime, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(entry.locationName, color = SoftLime, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPill(label = "P: ${entry.protein.toInt()}g", color = ProteinOrange)
                        MacroPill(label = "C: ${entry.carbs.toInt()}g", color = CarbYellow)
                        MacroPill(label = "F: ${entry.fats.toInt()}g", color = FatBlue)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${entry.calories.toInt()} kcal",
                        color = SoftLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_button_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove entry",
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroPill(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyLogsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = GrayText.copy(alpha = 0.3f),
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = "No food logs registered today",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Text(
            text = "Tap on a meal type above to scan foods visually with your camera, scan a barcode, or enter items manually.",
            color = GrayText,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun GoalEditorDialog(
    currentCal: Double,
    currentProt: Double,
    currentCarb: Double,
    currentFat: Double,
    currentThemeMode: String,
    currentDynamicColor: Boolean,
    todayActiveCalories: Double,
    onDismiss: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onSave: (Double, Double, Double, Double, String, Boolean) -> Unit
) {
    var calStr by remember { mutableStateOf(currentCal.toInt().toString()) }
    var protStr by remember { mutableStateOf(currentProt.toInt().toString()) }
    var carbStr by remember { mutableStateOf(currentCarb.toInt().toString()) }
    var fatStr by remember { mutableStateOf(currentFat.toInt().toString()) }
    var selectedTheme by remember { mutableStateOf(currentThemeMode) }
    var dynamicColor by remember { mutableStateOf(currentDynamicColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Settings & Targets", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Configure Daily Targets", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = calStr,
                        onValueChange = { calStr = it },
                        label = { Text("Calories", color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("goal_cal_input")
                    )
                    OutlinedTextField(
                        value = protStr,
                        onValueChange = { protStr = it },
                        label = { Text("Protein (g)", color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("goal_prot_input")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbStr,
                        onValueChange = { carbStr = it },
                        label = { Text("Carbs (g)", color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("goal_carbs_input")
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fats (g)", color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f).testTag("goal_fats_input")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)

                Text("Biometric Calibration", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Calorie Offset: +${todayActiveCalories.toInt()} kcal",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dilute macros based on today's active burn.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = {
                            val addedCals = todayActiveCalories
                            val nextCal = (calStr.toDoubleOrNull() ?: currentCal) + addedCals
                            val nextProt = (protStr.toDoubleOrNull() ?: currentProt) + (addedCals * 0.25 / 4.0)
                            val nextCarb = (carbStr.toDoubleOrNull() ?: currentCarb) + (addedCals * 0.50 / 4.0)
                            val nextFat = (fatStr.toDoubleOrNull() ?: currentFat) + (addedCals * 0.25 / 9.0)
                            
                            calStr = nextCal.toInt().toString()
                            protStr = nextProt.toInt().toString()
                            carbStr = nextCarb.toInt().toString()
                            fatStr = nextFat.toInt().toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(32.dp).testTag("calibrate_with_health_connect")
                    ) {
                        Text("Calibrate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)

                Text("Theme Mode", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val modes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                    modes.forEach { (modeKey, modeLabel) ->
                        val selected = selectedTheme == modeKey
                        OutlinedButton(
                            onClick = { selectedTheme = modeKey },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.weight(1f).testTag("theme_btn_$modeKey")
                        ) {
                            Text(modeLabel, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic M3 Wallpaper Theme", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Adapts theme hues based on device wallpaper background limits (Android 12+)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = { dynamicColor = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("dynamic_color_switch")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 1.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NutriLens Premium", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Configure unlimited scanning and detailed insights", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onNavigateToPaywall,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(36.dp).testTag("go_to_paywall_button")
                    ) {
                        Text("Manage Plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        calStr.toDoubleOrNull() ?: currentCal,
                        protStr.toDoubleOrNull() ?: currentProt,
                        carbStr.toDoubleOrNull() ?: currentCarb,
                        fatStr.toDoubleOrNull() ?: currentFat,
                        selectedTheme,
                        dynamicColor
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_goals_confirm_button")
            ) {
                Text("Save Settings", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ReorderableSectionWrapper(
    sectionId: String,
    title: String,
    viewModel: NutritionViewModel,
    sectionOrder: List<String>,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isDragging by remember { mutableStateOf(false) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val index = sectionOrder.indexOf(sectionId)
    val totalSections = sectionOrder.size
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )

    val measuredHeight = remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                if (!isDragging) {
                    measuredHeight.value = size.height
                }
            }
    ) {
        if (isDragging && measuredHeight.value > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { measuredHeight.value.toDp() })
                    .border(
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = SoftLime.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(Color.White.copy(alpha = 0.03f))
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .offset { IntOffset(0, offsetY.toInt()) }
                .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp), clip = false)
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isDragging) SoftLime.copy(alpha = 0.8f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                )
                .pointerInput(sectionId, index) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount.y
                            
                            val swapThreshold = 220f
                            if (offsetY > swapThreshold && index < totalSections - 1) {
                                viewModel.moveSectionDown(sectionId, commit = false)
                                offsetY -= swapThreshold
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            else if (offsetY < -swapThreshold && index > 0) {
                                viewModel.moveSectionUp(sectionId, commit = false)
                                offsetY += swapThreshold
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            offsetY = 0f
                            viewModel.commitSectionOrder()
                        },
                        onDragCancel = {
                            isDragging = false
                            offsetY = 0f
                            viewModel.commitSectionOrder()
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardDark.copy(alpha = 0.5f))
                                .testTag("drag_handle_${sectionId}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Hold and drag anywhere to reorder $title",
                                tint = if (isDragging) SoftLime else GrayText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = if (isDragging) SoftLime else GrayText
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun WeeklySummarySection(
    weeklyCalories: List<Float>,
    calorieGoal: Double,
    days: List<String>,
    todayActiveCalories: Double,
    onTrendsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrendsClick() }
            .testTag("weekly_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Calorie Trend",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Average daily intake: ${weeklyCalories.average().toInt()} kcal",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View details",
                    tint = SoftLime,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (weeklyCalories.isNotEmpty()) {
                val chartEntryModel = remember(weeklyCalories) {
                    entryModelOf(*weeklyCalories.map { it as Number }.toTypedArray())
                }
                
                val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value: Float, _ ->
                    days.getOrNull(value.toInt()) ?: ""
                }

                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = bottomAxisValueFormatter
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            }
        }
    }
}

@Composable
fun CompanionSyncSection(
    viewModel: NutritionViewModel,
    todayActiveCalories: Double
) {
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_sync_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Companion Sync Status",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (isOffline) "Running in Local Offline Mode" else "Connected to Cloud Sync",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isSyncing) Color.Yellow else if (isOffline) Color.Gray else HealthyGreen,
                            shape = CircleShape
                        )
                )
            }

            if (isSyncing) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Syncing logs to database...", color = SoftLime, fontSize = 11.sp)
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = SoftLime,
                        trackColor = Color.White.copy(alpha = 0.05f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Offline Mode",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isOffline,
                    onCheckedChange = { viewModel.toggleOfflineMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SlateBg,
                        checkedTrackColor = SoftLime,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = CardDark.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("offline_mode_switch")
                )
            }

            if (!isOffline && !isSyncing) {
                Button(
                    onClick = { viewModel.syncCachedLocalEntries() },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftLime),
                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("sync_button")
                ) {
                    Text("Sync Now", color = SlateBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

