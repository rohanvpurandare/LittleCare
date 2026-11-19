package com.littlecare.babytracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BabyChangingStation
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalDrink
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.ActivityType
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.FeedSubType
import com.littlecare.babytracker.data.PoopConsistency
import com.littlecare.babytracker.ui.components.ExportTriggerPill
import com.littlecare.babytracker.ui.components.LargeQuickActionButton
import com.littlecare.babytracker.ui.components.OledCard
import com.littlecare.babytracker.ui.theme.AccentFeed
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.AccentPee
import com.littlecare.babytracker.ui.theme.AccentPoop
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledBorder
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.TextPrimary
import com.littlecare.babytracker.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    profile: BabyProfile,
    todayLogs: List<ActivityLog>,
    recentLogs: List<ActivityLog>,
    onOpenExport: () -> Unit,
    onQuickPee: (onLogged: (ActivityLog) -> Unit) -> Unit,
    onLogPoop: (PoopConsistency, String) -> Unit,
    onLogFeed: (FeedSubType, Int, Long) -> Unit,
    onDeleteLog: (ActivityLog) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showFeedSheet by remember { mutableStateOf(false) }
    var showPoopSheet by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }

    val ageText = remember(profile.birthDateEpochDay) {
        val dob = LocalDate.ofEpochDay(profile.birthDateEpochDay)
        val totalDays = ChronoUnit.DAYS.between(dob, LocalDate.now()).coerceAtLeast(0)
        val weeks = totalDays / 7
        val days = totalDays % 7
        if (weeks == 0L) "$days days old" else "${weeks}w ${days}d old"
    }

    val feedTodayLogs = todayLogs.filter { it.type == ActivityType.FEED.name }
    val feedCount = feedTodayLogs.size
    val feedTotalMins = feedTodayLogs.sumOf { it.durationMinutes }
    val peeCount = todayLogs.count { it.type == ActivityType.PEE.name }
    val poopCount = todayLogs.count { it.type == ActivityType.POOP.name }

    fun formatElapsed(type: ActivityType): String {
        val last = recentLogs.firstOrNull { it.type == type.name } ?: return "No logs yet"
        val minsAgo = Duration.between(
            Instant.ofEpochMilli(last.timestampMillis),
            Instant.now()
        ).toMinutes().coerceAtLeast(0)
        val hrs = minsAgo / 60
        val mins = minsAgo % 60
        return when {
            minsAgo == 0L -> "Just now"
            hrs > 0 -> "${hrs}h ${mins}m ago"
            else -> "${mins}m ago"
        }
    }

    val lastFeedBadge = remember(recentLogs) { formatElapsed(ActivityType.FEED) }
    val lastPeeBadge = remember(recentLogs) { formatElapsed(ActivityType.PEE) }
    val lastPoopBadge = remember(recentLogs) { formatElapsed(ActivityType.POOP) }

    Scaffold(
        containerColor = OledBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
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
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$ageText • Born ${profile.birthTimeText}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AccentGold.copy(alpha = 0.9f)
                        )
                    }
                    ExportTriggerPill(onClick = onOpenExport)
                }
            }

            item {
                OledCard(glowColor = AccentGold) {
                    Text(
                        text = "TODAY'S RHYTHM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStat(
                            label = "Feeds (${feedTotalMins}m)",
                            value = feedCount.toString(),
                            color = AccentFeed
                        )
                        SummaryStat(
                            label = "Wet (Pee)",
                            value = peeCount.toString(),
                            color = AccentPee
                        )
                        SummaryStat(
                            label = "Dirty (Poop)",
                            value = poopCount.toString(),
                            color = AccentPoop
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Quick Log",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                LargeQuickActionButton(
                    title = "Feeding Time",
                    subtitle = "Live timer or quick Breast / Bottle / Formula log",
                    lastLoggedBadge = lastFeedBadge,
                    icon = Icons.Rounded.LocalDrink,
                    accentColor = AccentFeed,
                    onClick = { showFeedSheet = true }
                )
            }

            item {
                LargeQuickActionButton(
                    title = "Urination (1-Tap)",
                    subtitle = "Tap once to record wet diaper immediately",
                    lastLoggedBadge = lastPeeBadge,
                    icon = Icons.Rounded.WaterDrop,
                    accentColor = AccentPee,
                    onClick = {
                        onQuickPee { insertedLog ->
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message = "Urination logged",
                                    actionLabel = "Undo"
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    onDeleteLog(insertedLog)
                                }
                            }
                        }
                    }
                )
            }

            item {
                LargeQuickActionButton(
                    title = "Poopy Time",
                    subtitle = "Log stool consistency & optional care notes",
                    lastLoggedBadge = lastPoopBadge,
                    icon = Icons.Rounded.BabyChangingStation,
                    accentColor = AccentPoop,
                    onClick = { showPoopSheet = true }
                )
            }

            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentLogs.isEmpty()) {
                item {
                    OledCard {
                        Text(
                            text = "No logs yet. Tap any button above to record your baby's first activity.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(recentLogs, key = { it.id }) { log ->
                    ActivityLogRow(
                        log = log,
                        timeFormatter = timeFormatter,
                        onDelete = { onDeleteLog(log) }
                    )
                }
            }
        }
    }

    if (showFeedSheet) {
        FeedBottomSheet(
            onDismiss = { showFeedSheet = false },
            onSave = { type, durationMins, startMillis ->
                onLogFeed(type, durationMins, startMillis)
                showFeedSheet = false
            }
        )
    }

    if (showPoopSheet) {
        PoopBottomSheet(
            onDismiss = { showPoopSheet = false },
            onSave = { consistency, note ->
                onLogPoop(consistency, note)
                showPoopSheet = false
            }
        )
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun ActivityLogRow(
    log: ActivityLog,
    timeFormatter: DateTimeFormatter,
    onDelete: () -> Unit
) {
    val timeStr = remember(log.timestampMillis) {
        Instant.ofEpochMilli(log.timestampMillis)
            .atZone(ZoneId.systemDefault())
            .format(timeFormatter)
    }

    val (title, detail, accent) = when (log.type) {
        ActivityType.FEED.name -> Triple(
            "Feeding • ${log.subType}",
            "${log.durationMinutes} min",
            AccentFeed
        )
        ActivityType.POOP.name -> Triple(
            "Poop • ${log.subType}",
            log.note.ifBlank { "Recorded" },
            AccentPoop
        )
        else -> Triple(
            "Urination (Wet Diaper)",
            "Recorded",
            AccentPee
        )
    }

    OledCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$timeStr • $detail", fontSize = 14.sp, color = TextSecondary)
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete entry",
                    tint = TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedBottomSheet(
    onDismiss: () -> Unit,
    onSave: (FeedSubType, Int, Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(FeedSubType.BREAST) }
    var durationMinutes by remember { mutableIntStateOf(15) }

    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var timerStartMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000L)
            elapsedSeconds += 1
            durationMinutes = (elapsedSeconds / 60).coerceAtLeast(1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Log Feeding", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeedSubType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.label, fontSize = 16.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentFeed,
                            selectedLabelColor = OledBlack
                        )
                    )
                }
            }

            Surface(
                color = OledBlack,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, OledBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Live Feed Timer", fontSize = 13.sp, color = TextSecondary)
                        val mins = elapsedSeconds / 60
                        val secs = elapsedSeconds % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentFeed
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            if (!isTimerRunning && elapsedSeconds == 0) {
                                timerStartMillis = System.currentTimeMillis()
                            }
                            isTimerRunning = !isTimerRunning
                        },
                        modifier = Modifier.heightIn(min = 52.dp),
                        border = BorderStroke(1.dp, AccentFeed)
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = AccentFeed
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerRunning) "Pause" else "Start Timer",
                            color = AccentFeed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text("Or Select Duration (Minutes)", fontSize = 14.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 15, 20, 30, 45).forEach { mins ->
                    FilterChip(
                        selected = !isTimerRunning && durationMinutes == mins,
                        onClick = {
                            isTimerRunning = false
                            elapsedSeconds = 0
                            durationMinutes = mins
                        },
                        label = { Text("${mins}m") },
                        modifier = Modifier.heightIn(min = 48.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentFeed,
                            selectedLabelColor = OledBlack
                        )
                    )
                }
            }

            Button(
                onClick = {
                    val startTime = if (elapsedSeconds > 0) timerStartMillis else System.currentTimeMillis()
                    onSave(selectedType, durationMinutes, startTime)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentFeed, contentColor = OledBlack)
            ) {
                Text("Save Feed ($durationMinutes min)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PoopBottomSheet(
    onDismiss: () -> Unit,
    onSave: (PoopConsistency, String) -> Unit
) {
    var selectedConsistency by remember { mutableStateOf(PoopConsistency.NORMAL) }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = OledSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Log Poopy Time", style = MaterialTheme.typography.titleLarge)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PoopConsistency.entries.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            FilterChip(
                                selected = selectedConsistency == item,
                                onClick = { selectedConsistency = item },
                                label = { Text(item.label, fontSize = 15.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 52.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentPoop,
                                    selectedLabelColor = OledBlack
                                )
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("Optional note (e.g., color, diaper rash)", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPoop,
                    unfocusedBorderColor = OledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Button(
                onClick = { onSave(selectedConsistency, note) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPoop, contentColor = OledBlack)
            ) {
                Text("Save Poop Log", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
