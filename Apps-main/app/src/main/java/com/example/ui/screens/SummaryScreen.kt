package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.model.FoodEntry
import com.example.ui.components.*
import com.example.ui.viewmodel.NutritionViewModel
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class DailyTotals(
    val dayLabel: String,
    val dateLabel: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val entries: List<FoodEntry>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryScreen(
    navController: NavController,
    viewModel: NutritionViewModel
) {
    val allMeals by viewModel.allLoggedMeals.collectAsState()
    val calorieGoal by viewModel.calorieGoal.collectAsState()
    val proteinGoal by viewModel.proteinGoal.collectAsState()
    val carbsGoal by viewModel.carbsGoal.collectAsState()
    val fatsGoal by viewModel.fatsGoal.collectAsState()

    var showCalorieView by remember { mutableStateOf(false) } // False = Macros view, True = Calories view
    var selectedDayIndex by remember { mutableStateOf(6) } // Preset to today (index 6)

    val dailyTotals = remember(allMeals) {
        val list = mutableListOf<DailyTotals>()
        val formatterDay = java.text.SimpleDateFormat("EEE", Locale.getDefault())
        val formatterDate = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())

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

            list.add(
                DailyTotals(
                    dayLabel = formatterDay.format(cal.time),
                    dateLabel = formatterDate.format(cal.time),
                    calories = dayEntries.sumOf { it.calories },
                    protein = dayEntries.sumOf { it.protein },
                    carbs = dayEntries.sumOf { it.carbs },
                    fats = dayEntries.sumOf { it.fats },
                    entries = dayEntries
                )
            )
        }
        list
    }

    // Recalculate selected indices safely in case size issues
    if (selectedDayIndex >= dailyTotals.size) {
        selectedDayIndex = max(0, dailyTotals.size - 1)
    }

    // Compute weekly averages
    val avgCalories = dailyTotals.map { it.calories }.average()
    val avgProtein = dailyTotals.map { it.protein }.average()
    val avgCarbs = dailyTotals.map { it.carbs }.average()
    val avgFats = dailyTotals.map { it.fats }.average()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WEEKLY SUMMARY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("summary_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = SoftLime
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBg)
            )
        },
        containerColor = SlateBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Averages cards block
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeeklyStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Calories",
                        value = "${avgCalories.toInt()} kcal",
                        color = HealthyGreen,
                        subText = "Goal: ${calorieGoal.toInt()}"
                    )
                    WeeklyStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Protein",
                        value = "${avgProtein.toInt()}g",
                        color = ProteinOrange,
                        subText = "Goal: ${proteinGoal.toInt()}"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeeklyStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Carbs",
                        value = "${avgCarbs.toInt()}g",
                        color = CarbYellow,
                        subText = "Goal: ${carbsGoal.toInt()}"
                    )
                    WeeklyStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Fats",
                        value = "${avgFats.toInt()}g",
                        color = FatBlue,
                        subText = "Goal: ${fatsGoal.toInt()}"
                    )
                }
            }

            // Interactive Tab/toggle switcher
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val activeColor = ButtonDefaults.buttonColors(containerColor = SlateBg, contentColor = SoftLime)
                    val inactiveColor = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = GrayText)

                    Button(
                        onClick = { showCalorieView = false },
                        colors = if (!showCalorieView) activeColor else inactiveColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("macros_view_tab"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Macronutrients (g)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showCalorieView = true },
                        colors = if (showCalorieView) activeColor else inactiveColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("calories_view_tab"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Calories (kcal)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Main Visual Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showCalorieView) "Calorie Intake vs Goal" else "Macronutrient Intake Patterns",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Row(
                                modifier = Modifier
                                    .background(SlateBg, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GrayText,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Tap bars to inspect day",
                                    color = GrayText,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom interactive Canvas Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            WeeklyInteractiveChart(
                                data = dailyTotals,
                                showCalorieView = showCalorieView,
                                selectedIndex = selectedDayIndex,
                                onSelectIndex = { selectedDayIndex = it },
                                calorieGoal = calorieGoal,
                                maxCalValue = max(3000.0, dailyTotals.map { it.calories }.maxOrNull() ?: 2000.0),
                                maxGramValue = max(300.0, dailyTotals.map { it.protein + it.carbs + it.fats }.maxOrNull() ?: 200.0)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Legends Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showCalorieView) {
                                LegendItem(color = SoftLime, text = "Calories Logged")
                                Spacer(modifier = Modifier.width(16.dp))
                                LegendItem(color = Color.White.copy(alpha = 0.5f), text = "Target Limit")
                            } else {
                                LegendItem(color = ProteinOrange, text = "Protein")
                                Spacer(modifier = Modifier.width(10.dp))
                                LegendItem(color = CarbYellow, text = "Carbs")
                                Spacer(modifier = Modifier.width(10.dp))
                                LegendItem(color = FatBlue, text = "Fats")
                            }
                        }
                    }
                }
            }

            // Selected Day detail panel
            val currentDayTotals = dailyTotals[selectedDayIndex]
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_day_details_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Inspect Logs",
                                    fontWeight = FontWeight.Bold,
                                    color = SoftLime,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${currentDayTotals.dayLabel} • ${currentDayTotals.dateLabel}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${currentDayTotals.calories.toInt()} kcal",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "P ${currentDayTotals.protein.toInt()}g  C ${currentDayTotals.carbs.toInt()}g  F ${currentDayTotals.fats.toInt()}g",
                                    color = GrayText,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Display detailed item entries of that specific day
                        if (currentDayTotals.entries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No logs written for this day",
                                        color = GrayText,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { navController.navigate("dashboard") }) {
                                        Text("Go to Home to Log", color = SoftLime, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            var animateTrigger by remember { mutableStateOf(0) }
                            LaunchedEffect(selectedDayIndex) {
                                animateTrigger++
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                currentDayTotals.entries.forEachIndexed { index, entry ->
                                    var isVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(animateTrigger, entry.id) {
                                        isVisible = false
                                        delay(index * 45L)
                                        isVisible = true
                                    }
                                    AnimatedVisibility(
                                        visible = isVisible,
                                        enter = fadeIn(animationSpec = tween(250)) + slideInVertically(
                                            initialOffsetY = { 20 },
                                            animationSpec = tween(250, easing = EaseOutBack)
                                        ),
                                        exit = fadeOut(animationSpec = tween(150))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = SlateBg.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${entry.mealType.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} • P: ${entry.protein.toInt()}g / C: ${entry.carbs.toInt()}g / F: ${entry.fats.toInt()}g",
                                                    color = GrayText,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Text(
                                                text = "${entry.calories.toInt()} kcal",
                                                fontWeight = FontWeight.Bold,
                                                color = SoftLime,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WeeklyStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
    subText: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = GrayText,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subText,
                fontSize = 10.sp,
                color = GrayText
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = GrayText, fontSize = 11.sp)
    }
}

@Composable
fun WeeklyInteractiveChart(
    data: List<DailyTotals>,
    showCalorieView: Boolean,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    calorieGoal: Double,
    maxCalValue: Double,
    maxGramValue: Double
) {
    val df = remember { DecimalFormat("###,###") }
    
    // Smooth entry and toggle animation fraction
    val animVal = remember { Animatable(0f) }
    LaunchedEffect(showCalorieView, data) {
        animVal.snapTo(0f)
        animVal.animateTo(1f, animationSpec = tween(750, easing = EaseOutCubic))
    }

    // Touch feedback selection animation for each day
    val selectionFractions = List(7) { index ->
        animateFloatAsState(
            targetValue = if (index == selectedIndex) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy),
            label = "selection_fraction_$index"
        )
    }

    val currentSoftLime = SoftLime
    val currentHealthyGreen = HealthyGreen

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data, showCalorieView) {
                detectTapGestures { offset ->
                    val totalWidth = size.width
                    val widthPerElement = totalWidth / 7f
                    val clickedIndex = (offset.x / widthPerElement).toInt()
                    if (clickedIndex in 0..6) {
                        onSelectIndex(clickedIndex)
                    }
                }
            }
            .testTag("summary_chart_canvas")
    ) {
        val totalWidth = size.width
        val totalHeight = size.height
        val paddingLeft = 45.dp.toPx()
        val paddingBottom = 25.dp.toPx()
        val paddingTop = 15.dp.toPx()

        val chartWidth = totalWidth - paddingLeft
        val chartHeight = totalHeight - paddingBottom - paddingTop

        val progressFactor = animVal.value

        // 1. Draw horizontal gridlines (Y-axis)
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val ratio = i.toFloat() / gridLinesCount
            val y = paddingTop + chartHeight * (1f - ratio)

            // Dotted gridline
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(paddingLeft, y),
                end = Offset(totalWidth, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Y Axis Labels
            val valueText = if (showCalorieView) {
                "${df.format((maxCalValue * ratio).toInt())}k"
            } else {
                "${df.format((maxGramValue * ratio).toInt())}g"
            }

            // Draw text onto screen manually
            drawContext.canvas.nativeCanvas.drawText(
                valueText,
                10.dp.toPx(),
                y + 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#94A3B8")
                    textSize = 10.dp.toPx()
                    typeface = android.graphics.Typeface.SANS_SERIF
                }
            )
        }

        // 2. Draw Calorie Goal Guideline Line if in calories view
        if (showCalorieView) {
            val goalRatio = (calorieGoal / maxCalValue).toFloat()
            if (goalRatio in 0.0f..1.0f) {
                // Grow goals guideline alongside the bar entry values!
                val valY = paddingTop + chartHeight * (1f - goalRatio * progressFactor)
                drawLine(
                    color = Color.White.copy(alpha = 0.35f * progressFactor),
                    start = Offset(paddingLeft, valY),
                    end = Offset(totalWidth, valY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            }
        }

        // 3. Draw Columns for the 7 Days
        val spacePerDay = chartWidth / 7f
        val baseColumnWidth = spacePerDay * 0.45f

        data.forEachIndexed { index, dayData ->
            val selectFraction = selectionFractions[index].value
            
            val centerColX = paddingLeft + (spacePerDay * index) + (spacePerDay / 2f)
            // Gently scale column width up slightly when selected/hovered for tactile look
            val columnWidth = baseColumnWidth * (1f + 0.08f * selectFraction)
            val leftColX = centerColX - (columnWidth / 2f)

            // Animated Selection Background Ribbon
            if (selectFraction > 0f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.05f * selectFraction),
                    topLeft = Offset(centerColX - (spacePerDay / 2f), paddingTop),
                    size = Size(spacePerDay, chartHeight + 10.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }

            if (showCalorieView) {
                // Calorie Bar View
                val progressRatio = min(1.0, dayData.calories / maxCalValue).toFloat()
                val barHeight = chartHeight * progressRatio * progressFactor
                val barTopY = paddingTop + (chartHeight - barHeight)

                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        currentSoftLime.copy(alpha = 0.6f + 0.4f * selectFraction),
                        currentHealthyGreen.copy(alpha = 0.6f + 0.4f * selectFraction)
                    )
                )

                if (barHeight > 0) {
                    drawRoundRect(
                        brush = gradientBrush,
                        topLeft = Offset(leftColX, barTopY),
                        size = Size(columnWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                } else {
                    // Small ambient tick for 0 values
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(leftColX, paddingTop + chartHeight - 4.dp.toPx()),
                        size = Size(columnWidth, 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

            } else {
                // Stacked Macronutrients (Prot, Carbs, Fats) Bar View
                val totalMacros = dayData.protein + dayData.carbs + dayData.fats
                val proteinRatio = if (totalMacros > 0) dayData.protein / maxGramValue else 0.0
                val carbsRatio = if (totalMacros > 0) dayData.carbs / maxGramValue else 0.0
                val fatsRatio = if (totalMacros > 0) dayData.fats / maxGramValue else 0.0

                val opacity = 0.55f + 0.45f * selectFraction

                var currentHeightOffset = 0f

                // Fats segment (Bottom)
                val fatBarHeight = (chartHeight * fatsRatio * progressFactor).toFloat()
                if (fatBarHeight > 0) {
                    drawRoundRect(
                        color = FatBlue.copy(alpha = opacity),
                        topLeft = Offset(leftColX, paddingTop + chartHeight - fatBarHeight),
                        size = Size(columnWidth, fatBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    currentHeightOffset += fatBarHeight
                }

                // Carbs segment (Middle)
                val carbBarHeight = (chartHeight * carbsRatio * progressFactor).toFloat()
                if (carbBarHeight > 0) {
                    drawRoundRect(
                        color = CarbYellow.copy(alpha = opacity),
                        topLeft = Offset(leftColX, paddingTop + chartHeight - currentHeightOffset - carbBarHeight),
                        size = Size(columnWidth, carbBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    currentHeightOffset += carbBarHeight
                }

                // Protein segment (Top)
                val proteinBarHeight = (chartHeight * proteinRatio * progressFactor).toFloat()
                if (proteinBarHeight > 0) {
                    drawRoundRect(
                        color = ProteinOrange.copy(alpha = opacity),
                        topLeft = Offset(leftColX, paddingTop + chartHeight - currentHeightOffset - proteinBarHeight),
                        size = Size(columnWidth, proteinBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Small tick for no values
                if (totalMacros == 0.0) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.15f),
                        topLeft = Offset(leftColX, paddingTop + chartHeight - 4.dp.toPx()),
                        size = Size(columnWidth, 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // X-Axis Day Label (with dynamic bold selection)
            drawContext.canvas.nativeCanvas.drawText(
                dayData.dayLabel,
                centerColX,
                totalHeight - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (150 + 105 * selectFraction).toInt(),
                        255, 255, 255
                    )
                    textSize = 10.dp.toPx()
                    typeface = if (selectFraction > 0.5f) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun RowScope.WeeklyStatCardPlaceholder() {
    Card(
        modifier = Modifier
            .weight(1f)
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {}
}
