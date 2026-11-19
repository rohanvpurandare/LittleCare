package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.littlecare.babytracker.ui.theme.AccentFeed
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.TextPrimary
import com.littlecare.babytracker.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        name: String,
        birthDateEpochDay: Long,
        birthTimeText: String,
        birthWeightKg: Float?,
        birthHeightCm: Float?
    ) -> Unit
) {
    var babyName by remember { mutableStateOf("") }
    var birthDateEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var birthTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var birthWeightText by remember { mutableStateOf("") }
    var birthHeightText by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OledBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = AccentGold.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "LITTLECARE • PRIVATE & OFFLINE",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = AccentGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Welcome, Parent",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Set up your baby's profile once to unlock 1-tap feed & diaper logs, WHO growth curves, and Indian MoHFW immunization dates.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text("Baby's Name", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = babyName,
                onValueChange = { babyName = it },
                placeholder = { Text("e.g., Aarav or Diya", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = OledSurface,
                    unfocusedContainerColor = OledSurface,
                    focusedBorderColor = AccentGold,
                    unfocusedBorderColor = OledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1.15f)) {
                    Text("Birth Date", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clickable { showDatePicker = true },
                        color = OledSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, OledBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = LocalDate.ofEpochDay(birthDateEpochDay).format(dateFormatter),
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Icon(Icons.Rounded.CalendarMonth, contentDescription = "Select Birth Date", tint = AccentGold)
                        }
                    }
                }

                Column(modifier = Modifier.weight(0.85f)) {
                    Text("Birth Time", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clickable { showTimePicker = true },
                        color = OledSurface,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, OledBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = birthTime.format(timeFormatter),
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Icon(Icons.Rounded.AccessTime, contentDescription = "Select Birth Time", tint = AccentGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Birth Measurements (Optional — plots Week 0 on WHO Chart)",
                fontSize = 13.5.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = birthWeightText,
                    onValueChange = { birthWeightText = it },
                    placeholder = { Text("Weight (kg)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurface,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = AccentFeed,
                        unfocusedBorderColor = OledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = birthHeightText,
                    onValueChange = { birthHeightText = it },
                    placeholder = { Text("Height (cm)", color = TextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledSurface,
                        unfocusedContainerColor = OledSurface,
                        focusedBorderColor = AccentFeed,
                        unfocusedBorderColor = OledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (babyName.isNotBlank()) {
                        onComplete(
                            babyName.trim(),
                            birthDateEpochDay,
                            birthTime.format(timeFormatter),
                            birthWeightText.toFloatOrNull(),
                            birthHeightText.toFloatOrNull()
                        )
                    }
                },
                enabled = babyName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGold,
                    contentColor = OledBlack
                )
            ) {
                Text("Start Tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = LocalDate.ofEpochDay(birthDateEpochDay)
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        birthDateEpochDay = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toEpochDay()
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = birthTime.hour,
            initialMinute = birthTime.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = OledSurface,
            title = { Text("Select Birth Time", color = Color.White) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    birthTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = AccentGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
