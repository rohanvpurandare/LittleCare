package com.littlecare.babytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlecare.babytracker.data.AppDatabase
import com.littlecare.babytracker.data.BabyPreferences
import com.littlecare.babytracker.ui.BabyViewModel
import com.littlecare.babytracker.ui.components.ExportReportsBottomSheet
import com.littlecare.babytracker.ui.screens.GrowthScreen
import com.littlecare.babytracker.ui.screens.HomeScreen
import com.littlecare.babytracker.ui.screens.OnboardingScreen
import com.littlecare.babytracker.ui.screens.VaccineScreen
import com.littlecare.babytracker.ui.theme.AccentGold
import com.littlecare.babytracker.ui.theme.LittleCareTheme
import com.littlecare.babytracker.ui.theme.OledBlack
import com.littlecare.babytracker.ui.theme.OledSurface
import com.littlecare.babytracker.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(OledBlack.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(OledSurface.toArgb())
        )
        super.onCreate(savedInstanceState)

        val preferences = BabyPreferences(applicationContext)
        val database = AppDatabase.getInstance(applicationContext)
        val viewModelFactory = BabyViewModel.Factory(
            preferences = preferences,
            activityDao = database.activityDao(),
            growthDao = database.growthDao(),
            vaccineDao = database.vaccineDao()
        )

        setContent {
            LittleCareTheme {
                val babyViewModel: BabyViewModel = viewModel(factory = viewModelFactory)
                LittleCareApp(viewModel = babyViewModel)
            }
        }
    }
}

@Composable
private fun LittleCareApp(viewModel: BabyViewModel) {
    val context = LocalContext.current
    val profile by viewModel.babyProfile.collectAsState()
    val currentProfile = profile ?: return

    if (!currentProfile.isSetupComplete) {
        OnboardingScreen(
            onComplete = { name, dobEpochDay, birthTimeText, birthWeightKg, birthHeightCm ->
                viewModel.completeOnboarding(
                    name = name,
                    birthDateEpochDay = dobEpochDay,
                    birthTimeText = birthTimeText,
                    birthWeightKg = birthWeightKg,
                    birthHeightCm = birthHeightCm
                )
            }
        )
    } else {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        var showExportSheet by remember { mutableStateOf(false) }

        val todayLogs by viewModel.todayLogs.collectAsState()
        val recentLogs by viewModel.recentLogs.collectAsState()
        val growthEntries by viewModel.growthEntries.collectAsState()
        val vaccineRecords by viewModel.vaccineRecords.collectAsState()
        val isExporting by viewModel.isExporting.collectAsState()
        val lastExportResult by viewModel.lastExportResult.collectAsState()

        Scaffold(
            containerColor = OledBlack,
            bottomBar = {
                NavigationBar(
                    containerColor = OledSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(82.dp)
                ) {
                    val items = listOf(
                        Triple("Daily", Icons.Rounded.Home, 0),
                        Triple("Growth", Icons.Rounded.ShowChart, 1),
                        Triple("Vaccines", Icons.Rounded.Vaccines, 2)
                    )
                    items.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(26.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OledBlack,
                                selectedTextColor = AccentGold,
                                indicatorColor = AccentGold,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        profile = currentProfile,
                        todayLogs = todayLogs,
                        recentLogs = recentLogs,
                        onOpenExport = { showExportSheet = true },
                        onQuickPee = { callback -> viewModel.logPeeInstant(callback) },
                        onLogPoop = { consistency, note -> viewModel.logPoop(consistency, note) },
                        onLogFeed = { type, duration, start -> viewModel.logFeed(type, duration, start) },
                        onDeleteLog = { log -> viewModel.deleteActivityLog(log) }
                    )
                    1 -> GrowthScreen(
                        currentAgeWeeks = viewModel.calculateBabyAgeWeeks(currentProfile.birthDateEpochDay),
                        entries = growthEntries,
                        onOpenExport = { showExportSheet = true },
                        onAddEntry = { wk, w, h -> viewModel.addGrowthEntry(wk, w, h) },
                        onDeleteEntry = { entry -> viewModel.deleteGrowthEntry(entry) }
                    )
                    2 -> VaccineScreen(
                        vaccines = vaccineRecords,
                        onOpenExport = { showExportSheet = true },
                        onToggleVaccine = { code, isGiven, epochDay ->
                            viewModel.toggleVaccineAdministered(code, isGiven, epochDay)
                        }
                    )
                }
            }
        }

        if (showExportSheet) {
            ExportReportsBottomSheet(
                babyName = currentProfile.name,
                isExporting = isExporting,
                lastExportResult = lastExportResult,
                onExportExcel = { viewModel.exportToExcel(context, currentProfile) },
                onExportPdf = { viewModel.exportToPdf(context, currentProfile) },
                onDismiss = {
                    showExportSheet = false
                    viewModel.clearExportResult()
                }
            )
        }
    }
}
