package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.ui.viewmodel.LittleCareViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun VaccineScreen(
    baby: BabyProfile,
    viewModel: LittleCareViewModel
) {
    val vaccines by viewModel.vaccines.collectAsState()
    val dob = LocalDate.ofEpochDay(baby.birthDateEpochDay)
    val today = LocalDate.now()
    val dateFmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    val completedCount = vaccines.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = "Indian Immunization Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "MoHFW / IAP Guidelines · $completedCount of ${vaccines.size} completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(vaccines, key = { it.id }) { vaccine ->
            val dueDate = dob.plusDays(vaccine.dueOffsetDays.toLong())
            val isOverdue = !vaccine.isCompleted && dueDate.isBefore(today)
            val isDueSoon = !vaccine.isCompleted && !isOverdue && !dueDate.isAfter(today.plusDays(14))

            val statusText = when {
                vaccine.isCompleted -> "Given on ${vaccine.completedEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFmt) } ?: ""}"
                isOverdue -> "Overdue · Due ${dueDate.format(dateFmt)}"
                isDueSoon -> "Due Soon · ${dueDate.format(dateFmt)}"
                else -> "Due ${dueDate.format(dateFmt)}"
            }

            val statusColor = when {
                vaccine.isCompleted -> MaterialTheme.colorScheme.tertiary
                isOverdue -> MaterialTheme.colorScheme.error
                isDueSoon -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = vaccine.isCompleted,
                        onCheckedChange = { viewModel.toggleVaccineCompleted(vaccine) }
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                        Text(
                            text = "${vaccine.ageGroupLabel} · ${vaccine.name}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Protects against: ${vaccine.protectionAgainst}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
