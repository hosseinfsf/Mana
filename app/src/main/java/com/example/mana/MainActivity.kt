package com.example.mana

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mana.ui.onboarding.OnboardingScreen
import com.example.mana.ui.settings.SettingsScreen
import com.example.mana.ui.settings.SettingsViewModel
import com.example.mana.ui.settings.ThemeSetting
import com.example.mana.ui.theme.ManaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userDataPrefs = getSharedPreferences("mana_user_data", Context.MODE_PRIVATE)
        val isOnboardingComplete = userDataPrefs.getBoolean("ONBOARDING_COMPLETE", false)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            val useDarkTheme = when (settingsState.theme) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            ManaTheme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isOnboardingComplete) {
                        SettingsScreen(settingsViewModel)
                    } else {
                        OnboardingScreen(onOnboardingComplete = {
                            // A simple way to refresh the activity to show the settings screen
                            recreate()
                        })
                    }
                }
            }
        }
    }
}
