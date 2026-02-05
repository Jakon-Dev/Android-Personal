package com.example.androidpersonal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidpersonal.data.UserPreferences
import com.example.androidpersonal.data.finance.Wallet
import com.example.androidpersonal.data.finance.WalletType
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
                mutableStateOf(if (prefs.isDarkMode) true else false) 
            }
            
            val context = LocalContext.current
            
            var currentScreen by remember { 
                mutableStateOf(if (userName.isBlank()) Screen.Onboarding else Screen.Start) 
            }
            
            // Navigation State for Finance
            var selectedWallet by remember { mutableStateOf<Wallet?>(null) }

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
                                onAppClick = { appId ->
                                    if (appId == "finance") {
                                        currentScreen = Screen.FinanceHome
                                    } else {
                                        Toast.makeText(context, "$appId feature not implemented yet", Toast.LENGTH_SHORT).show()
                                    }
                                },
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
                        Screen.FinanceHome -> {
                            val financeRepo = remember { com.example.androidpersonal.data.finance.FinanceRepository(context) }
                            com.example.androidpersonal.ui.finance.home.FinanceHomeScreen(
                                repository = financeRepo,
                                onWalletClick = { wallet ->
                                    selectedWallet = wallet
                                    currentScreen = if (wallet.type == WalletType.NORMAL) Screen.FinanceNormalWallet else Screen.FinanceInvestmentWallet
                                },
                                onStatsClick = {
                                    currentScreen = Screen.FinanceStats
                                },
                                onBackClick = { currentScreen = Screen.Start }
                            )
                        }
                        Screen.FinanceNormalWallet -> {
                            val financeRepo = remember { com.example.androidpersonal.data.finance.FinanceRepository(context) }
                            val wallet = selectedWallet
                            if (wallet != null) {
                                com.example.androidpersonal.ui.finance.wallet.NormalWalletScreen(
                                    walletId = wallet.id,
                                    walletName = wallet.name,
                                    repository = financeRepo,
                                    onBackClick = { currentScreen = Screen.FinanceHome }
                                )
                            } else {
                                currentScreen = Screen.FinanceHome // Fallback
                            }
                        }
                        Screen.FinanceInvestmentWallet -> {
                            val financeRepo = remember { com.example.androidpersonal.data.finance.FinanceRepository(context) }
                            val wallet = selectedWallet
                            if (wallet != null) {
                                com.example.androidpersonal.ui.finance.wallet.InvestmentWalletScreen(
                                    walletId = wallet.id,
                                    walletName = wallet.name,
                                    repository = financeRepo,
                                    onBackClick = { currentScreen = Screen.FinanceHome }
                                )
                            } else {
                                currentScreen = Screen.FinanceHome
                            }
                        }
                        Screen.FinanceStats -> {
                            val financeRepo = remember { com.example.androidpersonal.data.finance.FinanceRepository(context) }
                            com.example.androidpersonal.ui.finance.stats.GeneralStatsScreen(
                                repository = financeRepo,
                                onBackClick = { currentScreen = Screen.FinanceHome }
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
    Settings,
    FinanceHome,
    FinanceNormalWallet,
    FinanceInvestmentWallet,
    FinanceStats
}