package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.domain.WhoGrowthReference
import com.littlecare.babytracker.ui.viewmodel.LittleCareViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun GrowthScreen(
    baby: BabyProfile,
    viewModel: LittleCareViewModel
) {
    val entries by viewModel.growthEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val dob = LocalDate.ofEpochDay(baby.birthDateEpochDay)
    val currentWeek = (ChronoUnit.DAYS.between(dob, LocalDate.now()).coerceAtLeast(0) / 7).toInt().coerceIn(0, 52)
    val isBoy = baby.gender.equals("Boy", ignoreCase = true)
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "1st Year Growth",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Weekly Weight & Height (Weeks 0–52) vs WHO 50th",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Log Week")
                }
            }
        }

        // WHO vs Actual Weight Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Weight Curve (kg) — Amber: ${baby.name} | Dashed Blue: WHO 50th", style = MaterialTheme.typography.labelLarge)
                    GrowthChartCanvas(
                        entries = entries,
                        isBoy = isBoy,
                        isWeight = true,
                        accentColor = MaterialTheme.colorScheme.primary,
                        refColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // WHO vs Actual Height Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Length / Height Curve (cm) — Mint: ${baby.name} | Dashed Blue: WHO 50th", style = MaterialTheme.typography.labelLarge)
                    GrowthChartCanvas(
                        entries = entries,
                        isBoy = isBoy,
                        isWeight = false,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        refColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        items(entries.sortedByDescending { it.weekNumber }, key = { it.id }) { entry ->
            val whoW = WhoGrowthReference.expectedWeightKg(entry.weekNumber, isBoy)
            val whoH = WhoGrowthReference.expectedHeightCm(entry.weekNumber, isBoy)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Week ${entry.weekNumber} · ${LocalDate.ofEpochDay(entry.recordedEpochDay).format(dateFmt)}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(
                                Locale.US,
                                "Weight: %.2f kg (WHO: %.2f kg) · Height: %.1f cm (WHO: %.1f cm)",
                                entry.weightKg, whoW, entry.heightCm, whoH
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (entry.weekNumber > 0) {
                        IconButton(onClick = { viewModel.deleteGrowthEntry(entry) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var weekText by remember { mutableStateOf(currentWeek.toString()) }
        val latest = entries.maxByOrNull { it.weekNumber }
        var weightText by remember { mutableStateOf((latest?.weightKg ?: baby.birthWeightKg).toString()) }
        var heightText by remember { mutableStateOf((latest?.heightCm ?: baby.birthHeightCm).toString()) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Weekly Growth") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weekText,
                        onValueChange = { weekText = it },
                        label = { Text("Week Number (0 to 52)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height / Length (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val wNum = weekText.toIntOrNull()?.coerceIn(0, 52) ?: currentWeek
                    val wt = weightText.toFloatOrNull()
                    val ht = heightText.toFloatOrNull()
                    if (wt != null && ht != null) {
                        viewModel.addGrowthEntry(wNum, wt, ht)
                        showAddDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GrowthChartCanvas(
    entries: List<GrowthEntry>,
    isBoy: Boolean,
    isWeight: Boolean,
    accentColor: Color,
    refColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(vertical = 8.dp)
    ) {
        val minVal = if (isWeight) 2.0f else 45.0f
        val maxVal = if (isWeight) 12.0f else 82.0f
        val w = size.width
        val h = size.height

        fun xForWeek(week: Int): Float = (week.coerceIn(0, 52) / 52f) * w
        fun yForVal(v: Float): Float = h - ((v - minVal) / (maxVal - minVal)).coerceIn(0f, 1f) * h

        // Subtle horizontal grid lines
        for (i in 0..4) {
            val y = (i / 4f) * h
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.5f
            )
        }

        // WHO 50th Reference Curve (0 to 52 weeks)
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        for (wk in 0 until 52 step 2) {
            val v0 = if (isWeight) WhoGrowthReference.expectedWeightKg(wk, isBoy) else WhoGrowthReference.expectedHeightCm(wk, isBoy)
            val v1 = if (isWeight) WhoGrowthReference.expectedWeightKg(wk + 2, isBoy) else WhoGrowthReference.expectedHeightCm(wk + 2, isBoy)
            drawLine(
                color = refColor.copy(alpha = 0.75f),
                start = Offset(xForWeek(wk), yForVal(v0)),
                end = Offset(xForWeek(wk + 2), yForVal(v1)),
                strokeWidth = 3f,
                pathEffect = dash
            )
        }

        // Baby's logged points
        val sorted = entries.sortedBy { it.weekNumber }
        for (i in 0 until sorted.lastIndex) {
            val p0 = sorted[i]
            val p1 = sorted[i + 1]
            val v0 = if (isWeight) p0.weightKg else p0.heightCm
            val v1 = if (isWeight) p1.weightKg else p1.heightCm
            drawLine(
                color = accentColor,
                start = Offset(xForWeek(p0.weekNumber), yForVal(v0)),
                end = Offset(xForWeek(p1.weekNumber), yForVal(v1)),
                strokeWidth = 5f
            )
        }
        sorted.forEach { p ->
            val v = if (isWeight) p.weightKg else p.heightCm
            drawCircle(
                color = accentColor,
                radius = 7f,
                center = Offset(xForWeek(p.weekNumber), yForVal(v))
            )
        }
    }
}
