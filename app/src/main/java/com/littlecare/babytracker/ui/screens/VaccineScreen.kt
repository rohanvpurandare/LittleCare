package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecare.babytracker.data.VaccineRecord
import com.littlecare.babytracker.ui.components.CircularRingProgress
import com.littlecare.babytracker.ui.components.ExportTriggerPill
import com.littlecare.babytracker.ui.components.OledCard
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.AccentOverdue
import com.littlecare.babytracker.ui.theme.AccentPee
import com.littlecare.babytracker.ui.theme.AccentVaccine
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineScreen(
    vaccines: List<VaccineRecord>,
    onOpenExport: () -> Unit,
    onToggleVaccine: (code: String, isAdministered: Boolean, administeredEpochDay: Long?) -> Unit
) {
    var selectedVaccineForDialog by remember { mutableStateOf<VaccineRecord?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var filterMode by remember { mutableIntStateOf(0) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    val completedCount = vaccines.count { it.isAdministered }
    val progress = if (vaccines.isEmpty()) 0f else completedCount.toFloat() / vaccines.size.toFloat()

    val filteredVaccines = remember(vaccines, filterMode) {
        when (filterMode) {
            1 -> vaccines.filter { !it.isAdministered }
            2 -> vaccines.filter { it.isAdministered }
            else -> vaccines
        }
    }

    val groupedVaccines = remember(filteredVaccines) {
        filteredVaccines.groupBy { it.stageLabel }
    }

    Scaffold(containerColor = OledBlack) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        CircularRingProgress(
                            progress = progress,
                            centerText = "$completedCount/${vaccines.size}",
                            accentColor = AccentVaccine
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Immunizations", style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = "Indian MoHFW & NMC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentGold
                            )
                        }
                    }
                    ExportTriggerPill(onClick = onOpenExport)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All (${vaccines.size})", "Pending", "Given ($completedCount)").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = filterMode == idx,
                            onClick = { filterMode = idx },
                            label = { Text(label, fontSize = 14.sp) },
                            modifier = Modifier.heightIn(min = 46.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentVaccine,
                                selectedLabelColor = OledBlack
                            )
                        )
                    }
                }
            }

            groupedVaccines.forEach { (stageLabel, stageVaccines) ->
                item(key = "header_$stageLabel") {
                    Text(
                        text = stageLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentVaccine,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }

                items(stageVaccines, key = { it.code }) { vaccine ->
                    val isOverdue = !vaccine.isAdministered && vaccine.dueDateEpochDay < todayEpochDay
                    val dueStr = LocalDate.ofEpochDay(vaccine.dueDateEpochDay).format(dateFormatter)
                    val givenStr = vaccine.administeredDateEpochDay?.let {
                        LocalDate.ofEpochDay(it).format(dateFormatter)
                    }

                    val borderColor = when {
                        vaccine.isAdministered -> AccentPee.copy(alpha = 0.55f)
                        isOverdue -> AccentOverdue.copy(alpha = 0.75f)
                        else -> OledBorder
                    }

                    OledCard(
                        borderColor = borderColor,
                        glowColor = if (isOverdue) AccentOverdue else null,
                        modifier = Modifier.clickable {
                            selectedVaccineForDialog = vaccine
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (vaccine.isAdministered) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.RadioButtonUnchecked
                                },
                                contentDescription = if (vaccine.isAdministered) "Administered" else "Pending",
                                tint = when {
                                    vaccine.isAdministered -> AccentPee
                                    isOverdue -> AccentOverdue
                                    else -> TextSecondary
                                },
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vaccine.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = vaccine.description,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                if (vaccine.isAdministered && givenStr != null) {
                                    StatusBadge(text = "Given on $givenStr", color = AccentPee)
                                } else if (isOverdue) {
                                    StatusBadge(text = "Overdue • Due $dueStr", color = AccentOverdue)
                                } else {
                                    StatusBadge(text = "Due: $dueStr", color = AccentVaccine)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedVaccineForDialog?.let { vaccine ->
        AlertDialog(
            onDismissRequest = { selectedVaccineForDialog = null },
            containerColor = OledSurface,
            title = { Text(vaccine.name, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(vaccine.description, color = TextSecondary, fontSize = 15.sp)

                    if (!vaccine.isAdministered) {
                        Button(
                            onClick = {
                                onToggleVaccine(vaccine.code, true, LocalDate.now().toEpochDay())
                                selectedVaccineForDialog = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPee,
                                contentColor = OledBlack
                            )
                        ) {
                            Text("Mark Given Today", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { showCustomDatePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentVaccine.copy(alpha = 0.2f),
                                contentColor = AccentVaccine
                            )
                        ) {
                            Text("Pick Administered Date", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                onToggleVaccine(vaccine.code, false, null)
                                selectedVaccineForDialog = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentOverdue.copy(alpha = 0.2f),
                                contentColor = AccentOverdue
                            )
                        ) {
                            Text("Unmark as Not Given", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedVaccineForDialog = null }) {
                    Text("Close", color = TextSecondary, fontSize = 16.sp)
                }
            }
        )
    }

    if (showCustomDatePicker && selectedVaccineForDialog != null) {
        val vaccine = selectedVaccineForDialog!!
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val chosenDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toEpochDay()
                        onToggleVaccine(vaccine.code, true, chosenDay)
                    }
                    showCustomDatePicker = false
                    selectedVaccineForDialog = null
                }) {
                    Text("Save Date", color = AccentPee, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
