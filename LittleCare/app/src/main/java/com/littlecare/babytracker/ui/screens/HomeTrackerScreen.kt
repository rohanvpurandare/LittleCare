package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.ui.viewmodel.LittleCareViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun HomeTrackerScreen(
    baby: BabyProfile,
    allBabies: List<BabyProfile>,
    viewModel: LittleCareViewModel,
    onAddNewBaby: () -> Unit
) {
    val logs by viewModel.activityLogs.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val timerLabel by viewModel.timerLabel.collectAsState()
    val nightVision by viewModel.nightVisionMode.collectAsState()

    var showBottleDialog by remember { mutableStateOf(false) }
    var showMedsDialog by remember { mutableStateOf(false) }

    val dob = LocalDate.ofEpochDay(baby.birthDateEpochDay)
    val today = LocalDate.now()
    val ageDays = ChronoUnit.DAYS.between(dob, today).coerceAtLeast(0)
    val ageWeeks = ageDays / 7
    val ageMonths = ChronoUnit.MONTHS.between(dob, today)

    val timeFmt = remember {
        DateTimeFormatter.ofPattern("hh:mm a · dd MMM").withZone(ZoneId.systemDefault())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Night Vision Toggle + Multi-Baby Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = baby.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$ageMonths mo · $ageWeeks wks ($ageDays days)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleNightVision(!nightVision) }) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Toggle Night Vision",
                            tint = if (nightVision) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onAddNewBaby) {
                        Icon(Icons.Default.Add, contentDescription = "Add Baby")
                    }
                }
            }

            if (allBabies.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    items(allBabies) { profile ->
                        FilterChip(
                            selected = profile.id == baby.id,
                            onClick = { viewModel.selectActiveBaby(profile.id) },
                            label = { Text(profile.name) }
                        )
                    }
                }
            }
        }

        // Live Feeding & Sleep Timer Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Live Session Timer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Breast (Left)", "Breast (Right)", "Sleep").forEach { mode ->
                            FilterChip(
                                selected = timerLabel == mode,
                                onClick = { viewModel.selectTimerLabel(mode) },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mins = timerSeconds / 60
                        val secs = timerSeconds % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.toggleTimer() },
                                modifier = Modifier.height(52.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (timerRunning) "Pause" else "Start")
                            }

                            if (timerSeconds > 0) {
                                FilledTonalButton(
                                    onClick = { viewModel.saveAndResetTimer() },
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }

        // One-Tap Quick Actions (Feeding, Diapers, Meds)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Quick 1-Tap Log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showBottleDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Bottle Feed")
                        }
                        FilledTonalButton(
                            onClick = { showMedsDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Meds / Temp")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.logQuickActivity("DIAPER", "Wet Diaper") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Wet")
                        }
                        OutlinedButton(
                            onClick = { viewModel.logQuickActivity("DIAPER", "Dirty Diaper") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dirty")
                        }
                        OutlinedButton(
                            onClick = { viewModel.logQuickActivity("DIAPER", "Wet + Dirty") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Both")
                        }
                    }
                }
            }
        }

        // Recent Activity Feed
        item {
            Text(
                text = "Recent Logs (${logs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "No entries yet. Use the timer or 1-tap buttons above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logs.take(30), key = { it.id }) { log ->
                ActivityLogRow(
                    log = log,
                    formattedTime = timeFmt.format(Instant.ofEpochMilli(log.timestampMillis)),
                    onDelete = { viewModel.deleteActivity(log) }
                )
            }
        }
    }

    if (showBottleDialog) {
        var volumeMl by remember { mutableFloatStateOf(90f) }
        var feedType by remember { mutableStateOf("Formula") }
        AlertDialog(
            onDismissRequest = { showBottleDialog = false },
            title = { Text("Log Bottle Feed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Breastmilk", "Formula").forEach { type ->
                            FilterChip(
                                selected = feedType == type,
                                onClick = { feedType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    Text("Volume: ${volumeMl.toInt()} ml", fontWeight = FontWeight.Bold)
                    Slider(
                        value = volumeMl,
                        onValueChange = { volumeMl = it },
                        valueRange = 10f..250f,
                        steps = 23
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logQuickActivity("FEED", "Bottle ($feedType)", amountMl = volumeMl.toInt())
                    showBottleDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showBottleDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMedsDialog) {
        var medName by remember { mutableStateOf("Vitamin D3 Drops") }
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showMedsDialog = false },
            title = { Text("Log Medication / Temp") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medication / Reading") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Dose / Temp (e.g. 0.5 ml or 98.4°F)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (medName.isNotBlank()) {
                        viewModel.logQuickActivity("MEDS", medName, notes = note)
                    }
                    showMedsDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showMedsDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ActivityLogRow(
    log: ActivityLog,
    formattedTime: String,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val detail = buildString {
                    append(log.subType)
                    if (log.amountMl > 0) append(" · ${log.amountMl} ml")
                    if (log.durationSeconds > 0) {
                        val m = log.durationSeconds / 60
                        val s = log.durationSeconds % 60
                        append(" · ${m}m ${s}s")
                    }
                    if (log.notes.isNotBlank()) append(" (${log.notes})")
                }
                Text(detail, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(formattedTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
            }
        }
    }
}
