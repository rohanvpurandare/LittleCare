package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.domain.WhoGrowthStandards
import com.littlecare.babytracker.ui.components.ExportTriggerPill
import com.littlecare.babytracker.ui.components.GrowthChart
import com.littlecare.babytracker.ui.components.OledCard
import com.littlecare.babytracker.ui.theme.AccentFeed
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.AccentPee
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.TextPrimary
import com.littlecare.babytracker.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun GrowthScreen(
    currentAgeWeeks: Int,
    entries: List<GrowthEntry>,
    onOpenExport: () -> Unit,
    onAddEntry: (week: Int, weightKg: Float?, heightCm: Float?) -> Unit,
    onDeleteEntry: (GrowthEntry) -> Unit
) {
    var showWeightTab by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    val chartPoints = remember(entries, showWeightTab) {
        entries.mapNotNull { entry ->
            val value = if (showWeightTab) entry.weightKg else entry.heightCm
            value?.let { entry.weekNumber to it }
        }
    }

    val whoMedianAtCurrentWeek = remember(currentAgeWeeks, showWeightTab) {
        WhoGrowthStandards.getInterpolatedMedian(currentAgeWeeks, isWeight = showWeightTab)
    }

    Scaffold(containerColor = OledBlack) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Growth Tracker", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Week $currentAgeWeeks of 52",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentGold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExportTriggerPill(onClick = onOpenExport)
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.heightIn(min = 44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentFeed,
                                contentColor = OledBlack
                            )
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = showWeightTab,
                        onClick = { showWeightTab = true },
                        label = { Text("Weight (kg)", fontSize = 16.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentFeed,
                            selectedLabelColor = OledBlack
                        )
                    )
                    FilterChip(
                        selected = !showWeightTab,
                        onClick = { showWeightTab = false },
                        label = { Text("Height (cm)", fontSize = 16.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPee,
                            selectedLabelColor = OledBlack
                        )
                    )
                }
            }

            item {
                GrowthChart(
                    referenceBand = if (showWeightTab) WhoGrowthStandards.weightReference else WhoGrowthStandards.heightReference,
                    babyDataPoints = chartPoints,
                    unitLabel = if (showWeightTab) "kg" else "cm",
                    accentColor = if (showWeightTab) AccentFeed else AccentPee
                )
            }

            item {
                OledCard {
                    val unit = if (showWeightTab) "kg" else "cm"
                    val formattedMedian = String.format(Locale.US, "%.1f", whoMedianAtCurrentWeek)
                    Text(
                        text = "WHO Week $currentAgeWeeks Median: $formattedMedian $unit",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Healthy babies follow their own steady curve. Consult your paediatrician if growth crosses two percentile lines.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Text("Recorded Measurements", style = MaterialTheme.typography.titleMedium)
            }

            if (entries.isEmpty()) {
                item {
                    OledCard {
                        Text(
                            "No weight or height measurements logged yet. Tap '+ Log' above to add birth or weekly readings.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(entries.asReversed(), key = { it.id }) { entry ->
                    val dateStr = LocalDate.ofEpochDay(entry.recordedDateEpochDay).format(dateFormatter)
                    val wStr = entry.weightKg?.let { "${it} kg" } ?: "—"
                    val hStr = entry.heightCm?.let { "${it} cm" } ?: "—"

                    OledCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Week ${entry.weekNumber} • $dateStr",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Weight: $wStr   |   Height: $hStr",
                                    fontSize = 15.sp,
                                    color = AccentFeed
                                )
                            }
                            IconButton(
                                onClick = { onDeleteEntry(entry) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddGrowthDialog(
            defaultWeek = currentAgeWeeks,
            onDismiss = { showAddDialog = false },
            onSave = { wk, w, h ->
                onAddEntry(wk, w, h)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddGrowthDialog(
    defaultWeek: Int,
    onDismiss: () -> Unit,
    onSave: (week: Int, weightKg: Float?, heightCm: Float?) -> Unit
) {
    var weekText by remember { mutableStateOf(defaultWeek.toString()) }
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OledSurface,
        title = { Text("Log Weekly Growth", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weekText,
                    onValueChange = { weekText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Baby's Age in Weeks (0–52)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentFeed,
                        unfocusedBorderColor = OledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight in kg (e.g., 4.25)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentFeed,
                        unfocusedBorderColor = OledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height / Length in cm (e.g., 54.5)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPee,
                        unfocusedBorderColor = OledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val wk = (weekText.toIntOrNull() ?: defaultWeek).coerceIn(0, 52)
                val w = weightText.toFloatOrNull()
                val h = heightText.toFloatOrNull()
                if (w != null || h != null) {
                    onSave(wk, w, h)
                }
            }) {
                Text("Save", color = AccentFeed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontSize = 16.sp)
            }
        }
    )
}
