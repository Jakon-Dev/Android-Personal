package com.example.androidpersonal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidpersonal.data.UserPreferences
import com.example.androidpersonal.ui.menu.StartMenu
import com.example.androidpersonal.ui.onboarding.OnboardingScreen
import com.example.androidpersonal.ui.settings.SettingsScreen
import com.example.androidpersonal.ui.theme.AndroidPersonalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val prefs = UserPreferences(this)
        
        setContent {
            // State persistence
            var userName by remember { mutableStateOf(prefs.userName) }
            var isDarkMode by remember { 
                mutableStateOf(if (prefs.isDarkMode) true else false) // Start with basic check, improve if needed to match system default handling
            }
            
            // If explicit dark mode pref is not set, we could fallback to system, 
            // but for now we rely on the Boolean implementation in UserPreferences which defaults to false (light).
            // A better approach would be storing a nullable Boolean? or Enum. 
            // Given the requirement "change option", let's strictly follow the switch. 
            // However, on first run, we might want to respect system.
            // Let's rely on the passed `isDarkMode` state for the theme.
            
            var currentScreen by remember { 
                mutableStateOf(if (userName.isBlank()) Screen.Onboarding else Screen.Start) 
            }

            // Sync with prefs when state changes
            LaunchedEffect(isDarkMode) {
                prefs.isDarkMode = isDarkMode
            }
            
            AndroidPersonalTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.Onboarding -> {
                            OnboardingScreen(
                                onContinue = { newName ->
                                    userName = newName
                                    prefs.userName = newName
                                    currentScreen = Screen.Start
                                }
                            )
                        }
                        Screen.Start -> {
                            StartMenu(
                                userName = userName,
                                modifier = Modifier.padding(innerPadding),
                                onStartClick = { /* Navigate to app functionality */ },
                                onSettingsClick = { currentScreen = Screen.Settings }
                            )
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                currentName = userName,
                                isDarkMode = isDarkMode,
                                onNameChange = { newName ->
                                    userName = newName
                                    prefs.userName = newName
                                },
                                onDarkModeChange = { isDarkMode = it },
                                onBackClick = { currentScreen = Screen.Start }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    Onboarding,
    Start,
    Settings
}