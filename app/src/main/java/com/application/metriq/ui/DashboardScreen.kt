package com.application.metriq.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.application.metriq.ui.theme.CardBackground
import com.application.metriq.ui.theme.CardBackgroundFaded
import com.application.metriq.ui.theme.MetriqTheme
import com.application.metriq.ui.theme.NutrientCarbs
import com.application.metriq.ui.theme.NutrientFats
import com.application.metriq.ui.theme.NutrientProtein
import com.application.metriq.ui.theme.PrimaryGreen
import com.application.metriq.ui.theme.TextGray
import com.application.metriq.ui.theme.TextWhite
import com.application.metriq.ui.theme.WorkoutBlue
import com.application.metriq.viewmodel.DailyNutrients
import com.application.metriq.viewmodel.FoodNutritionViewModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: FoodNutritionViewModel = viewModel()
) {
    val historicalData by viewModel.historicalData.collectAsState()
    val dailyTotals by viewModel.dailyTotals.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NutritionAnalyticsCard(historicalData = historicalData)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MacroDistributionCard(dailyTotals = dailyTotals)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Other Dashboard Content")
        }
    }
}

@Composable
fun NutritionAnalyticsCard(historicalData: Map<LocalDate, Double>) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.BarChart,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Last 7 Days Intake",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextGray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    BarChart(data = historicalData)
                }
            }
        }
    }
}

@Composable
fun BarChart(data: Map<LocalDate, Double>) {
    val maxCalories = data.values.maxOrNull()?.coerceAtLeast(2000.0) ?: 2000.0
    val days = (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
    val dateFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { date ->
            val calories = data[date] ?: 0.0
            val heightFraction = (calories / maxCalories).coerceIn(0.0, 1.0).toFloat()
            val isGoalMet = calories >= 2000 // Example goal
            val barColor = if (isGoalMet) PrimaryGreen else WorkoutBlue

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                if (calories > 0) {
                    Text(
                        text = calories.toInt().toString(),
                        fontSize = 10.sp,
                        color = TextGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(heightFraction.coerceAtLeast(0.01f)) // Ensure visible if > 0
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (calories > 0) barColor else CardBackgroundFaded)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = date.format(dateFormatter),
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun MacroDistributionCard(dailyTotals: DailyNutrients) {
    var expanded by remember { mutableStateOf(true) }
    // Standard daily values based on a 2000 kcal diet (FDA)
    val dailyProtein = 50.0
    val dailyCarbs = 275.0
    val dailyFats = 78.0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PieChart,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Macronutrient Distribution",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextGray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        PieChart(
                            dailyTotals = dailyTotals,
                            modifier = Modifier.size(120.dp)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MacroLegendItem("Protein", dailyTotals.protein, dailyProtein, NutrientProtein)
                            MacroLegendItem("Carbs", dailyTotals.carbs, dailyCarbs, NutrientCarbs)
                            MacroLegendItem("Fats", dailyTotals.fats, dailyFats, NutrientFats)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(dailyTotals: DailyNutrients, modifier: Modifier = Modifier) {
    val total = dailyTotals.protein + dailyTotals.carbs + dailyTotals.fats
    
    val proteinAngle = if (total > 0) (dailyTotals.protein / total * 360).toFloat() else 0f
    val carbsAngle = if (total > 0) (dailyTotals.carbs / total * 360).toFloat() else 0f
    val fatsAngle = if (total > 0) (dailyTotals.fats / total * 360).toFloat() else 0f

    Canvas(modifier = modifier) {
        var startAngle = -90f
        
        if (total == 0.0) {
            drawCircle(color = TextGray.copy(alpha = 0.2f))
        } else {
            drawArc(
                color = NutrientProtein,
                startAngle = startAngle,
                sweepAngle = proteinAngle,
                useCenter = true
            )
            startAngle += proteinAngle
            
            drawArc(
                color = NutrientCarbs,
                startAngle = startAngle,
                sweepAngle = carbsAngle,
                useCenter = true
            )
            startAngle += carbsAngle
            
            drawArc(
                color = NutrientFats,
                startAngle = startAngle,
                sweepAngle = fatsAngle,
                useCenter = true
            )
        }
    }
}

@Composable
fun MacroLegendItem(
    name: String,
    amount: Double,
    dailyValue: Double,
    color: Color
) {
    val percentage = if (dailyValue > 0) (amount / dailyValue * 100).toInt() else 0
    val df = DecimalFormat("#.##")
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextWhite, fontWeight = FontWeight.Bold)) {
                    append("$name: ")
                }
                withStyle(style = SpanStyle(color = TextGray)) {
                    append("${df.format(amount)}g ")
                }
                withStyle(style = SpanStyle(color = color, fontWeight = FontWeight.Bold)) {
                    append("($percentage% DV)")
                }
            },
            fontSize = 12.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MetriqTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
