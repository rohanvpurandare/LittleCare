package com.littlecare.babytracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.littlecare.babytracker.ui.screens.GrowthScreen
import com.littlecare.babytracker.ui.screens.HomeTrackerScreen
import com.littlecare.babytracker.ui.screens.OnboardingScreen
import com.littlecare.babytracker.ui.screens.VaccineScreen
import com.littlecare.babytracker.ui.screens.VaultAndMoreScreen
import com.littlecare.babytracker.ui.theme.LittleCareTheme
import com.littlecare.babytracker.ui.viewmodel.LittleCareViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: LittleCareViewModel = viewModel()
            val nightVision by vm.nightVisionMode.collectAsState()
            val biometricEnabled by vm.biometricLockEnabled.collectAsState()
            var isUnlocked by remember { mutableStateOf(false) }

            LaunchedEffect(biometricEnabled) {
                if (!biometricEnabled) {
                    isUnlocked = true
                } else if (!isUnlocked) {
                    promptBiometric { isUnlocked = true }
                }
            }

            LittleCareTheme(nightVisionMode = nightVision) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (biometricEnabled && !isUnlocked) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Button(onClick = { promptBiometric { isUnlocked = true } }) {
                                Text("Unlock LittleCare")
                            }
                        }
                    } else {
                        LittleCareApp(vm)
                    }
                }
            }
        }
    }

    private fun promptBiometric(onSuccess: () -> Unit) {
        val manager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            onSuccess()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock LittleCare")
            .setSubtitle("Verify identity to access offline baby health records")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }
}

@Composable
private fun LittleCareApp(viewModel: LittleCareViewModel) {
    val profiles by viewModel.profiles.collectAsState()
    val activeBaby by viewModel.activeBaby.collectAsState()
    var addingBaby by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    if (profiles.isEmpty() || addingBaby || activeBaby == null) {
        OnboardingScreen(
            onSaveProfile = { name, dob, gender, wt, ht ->
                viewModel.createBabyProfile(name, dob, gender, wt, ht)
                addingBaby = false
            },
            onCancel = if (profiles.isNotEmpty()) ({ addingBaby = false }) else null
        )
        return
    }

    val baby = activeBaby!!

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ChildCare, contentDescription = "Care") },
                    label = { Text("Care") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Growth") },
                    label = { Text("Growth") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Vaccines, contentDescription = "Vaccines") },
                    label = { Text("Vaccines") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "More") },
                    label = { Text("More") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeTrackerScreen(
                    baby = baby,
                    allBabies = profiles,
                    viewModel = viewModel,
                    onAddNewBaby = { addingBaby = true }
                )
                1 -> GrowthScreen(baby = baby, viewModel = viewModel)
                2 -> VaccineScreen(baby = baby, viewModel = viewModel)
                3 -> VaultAndMoreScreen(baby = baby, viewModel = viewModel)
            }
        }
    }
}
