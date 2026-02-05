package com.example.androidpersonal.ui.finance.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.androidpersonal.data.finance.FinanceRepository
import com.example.androidpersonal.data.finance.Wallet
import com.example.androidpersonal.data.finance.WalletType
import com.example.androidpersonal.ui.theme.OrangeAccent

@Composable
fun FinanceHomeScreen(
    repository: FinanceRepository,
    onWalletClick: (Wallet) -> Unit,
    onStatsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var wallets by remember { mutableStateOf(listOf<Wallet>()) }
    var totalNetWorth by remember { mutableDoubleStateOf(0.0) }
    var showAddWalletDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        wallets = repository.getWallets()
        totalNetWorth = repository.getTotalNetWorth()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWalletDialog = true },
                containerColor = OrangeAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Wallet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header - Net Worth
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Net Worth",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = String.format("$%.2f", totalNetWorth),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Button(
                onClick = onStatsClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("View General Statistics")
            }

            Text(
                text = "My Wallets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(wallets) { wallet ->
                    WalletCard(
                        wallet = wallet,
                        repository = repository, // Pass repo to fetch specific balance/stats inside card
                        onClick = { onWalletClick(wallet) }
                    )
                }
            }
        }
    }

    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onSave = { name, type ->
                repository.addWallet(name, type)
                // Refresh
                wallets = repository.getWallets()
                totalNetWorth = repository.getTotalNetWorth()
                showAddWalletDialog = false
            }
        )
    }
}

@Composable
fun WalletCard(
    wallet: Wallet,
    repository: FinanceRepository,
    onClick: () -> Unit
) {
    var balance by remember { mutableDoubleStateOf(0.0) }
    var performance by remember { mutableStateOf<Pair<Double, Double>?>(null) } // Profit, %

    LaunchedEffect(wallet) {
        if (wallet.type == WalletType.NORMAL) {
            balance = repository.getWalletBalance(wallet.id)
        } else {
            // For investment, we might show current value or profit
            // Let's show Current Value as "balance"
            val snapshots = repository.getSnapshots(wallet.id)
            balance = if (snapshots.isNotEmpty()) snapshots.last().totalValue else repository.getWalletBalance(wallet.id)
            performance = repository.getInvestmentPerformance(wallet.id)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (wallet.type == WalletType.INVESTMENT) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (wallet.type == WalletType.NORMAL) "Normal Wallet" else "Investment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("$%.2f", balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (wallet.type == WalletType.INVESTMENT && performance != null) {
                    val (profit, percent) = performance!!
                    Text(
                        text = String.format("%+.2f (%.1f%%)", profit, percent),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (profit >= 0) Color.Green else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onSave: (String, WalletType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.NORMAL) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create New Wallet", style = MaterialTheme.typography.headlineSmall)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Type", style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == WalletType.NORMAL,
                        onClick = { selectedType = WalletType.NORMAL },
                        label = { Text("Normal") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == WalletType.INVESTMENT,
                        onClick = { selectedType = WalletType.INVESTMENT },
                        label = { Text("Investment") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { if (name.isNotBlank()) onSave(name, selectedType) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank()
                ) {
                    Text("Create Wallet")
                }
            }
        }
    }
}
