package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FoodLog
import com.example.domain.model.MealType

val CalorieOrange = Color(0xFFE65100)    // High contrast orange

@Composable
fun CircularNutritionProgress(
    caloriesConsumed: Double,
    calorieGoal: Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp
) {
    val remaining = (calorieGoal - caloriesConsumed).coerceAtLeast(0.0)
    val progress = if (calorieGoal > 0) (caloriesConsumed / calorieGoal).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "calorieProgress"
    )

    val hGreen = HealthyGreen
    val sLime = SoftLime

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(190.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width - strokeWidth.toPx()) / 2

            // Background arc (soft neutral)
            drawArc(
                color = Color(0xFFECEFF1).copy(alpha = 0.15f),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )

            // Active calorie progress arc (Gradient)
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(hGreen, sLime)
                ),
                startAngle = -220f,
                sweepAngle = animatedProgress * 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }

        // Inner typography labels
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = "Calorie Icon",
                tint = CalorieOrange,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.0f", caloriesConsumed),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                ),
                color = Color.White
            )
            Text(
                text = "of %.0f kcal".format(calorieGoal),
                style = MaterialTheme.typography.bodySmall,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = if (remaining > 0) HealthyGreen.copy(alpha = 0.2f) else CalorieOrange.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = if (remaining > 0) "%.0f kcal left".format(remaining) else "Goal Reached! 🎉",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (remaining > 0) SoftLime else Color.White
                )
            }
        }
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    value: Double,
    goal: Double,
    color: Color,
    unit: String = "g",
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (value / goal).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "macroProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Text(
                text = "%.1f%s / %.0f%s".format(value, unit, goal, unit),
                style = MaterialTheme.typography.bodySmall,
                color = GrayText
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedProgress)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
fun MealGroupHeader(
    mealType: MealType,
    caloriesLogged: Double,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
            Text(
                text = "%.0f kcal".format(caloriesLogged),
                style = MaterialTheme.typography.bodySmall,
                color = SoftLime,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(
            onClick = onAddClicked,
            modifier = Modifier
                .background(HealthyGreen.copy(alpha = 0.15f), CircleShape)
                .size(36.dp)
                .testTag("add_to_${mealType.name.lowercase()}_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Log to ${mealType.displayName}",
                tint = SoftLime,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FoodLogItemCard(
    log: FoodLog,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.1f %s".format(log.servingSize, log.servingUnit),
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        color = GrayText,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "P: %.1fg, C: %.1fg, F: %.1fg".format(log.protein, log.carbs, log.fats),
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftLime
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.0f kcal".format(log.calories),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Meal",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
