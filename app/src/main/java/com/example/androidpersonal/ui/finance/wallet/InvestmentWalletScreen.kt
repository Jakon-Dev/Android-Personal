package com.example.androidpersonal.ui.finance.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.androidpersonal.data.finance.FinanceRepository
import com.example.androidpersonal.data.finance.InvestmentSnapshot
import com.example.androidpersonal.data.finance.Transaction
import com.example.androidpersonal.ui.theme.OrangeAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentWalletScreen(
    walletId: Long,
    walletName: String,
    repository: FinanceRepository,
    onBackClick: () -> Unit
) {
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var snapshots by remember { mutableStateOf(listOf<InvestmentSnapshot>()) }
    var investedAmount by remember { mutableDoubleStateOf(0.0) }
    var currentValuation by remember { mutableDoubleStateOf(0.0) }
    var showCapitalDialog by remember { mutableStateOf(false) }
    var showValuationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(walletId) {
        refreshData(walletId, repository) { t, s, i, c ->
            transactions = t
            snapshots = s
            investedAmount = i
            currentValuation = c
        }
    }
    
    val profit = currentValuation - investedAmount
    val profitPercent = if (investedAmount != 0.0) (profit / investedAmount) * 100 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(walletName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Stats Header
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Current Value", style = MaterialTheme.typography.labelMedium)
                            Text(String.format("$%.2f", currentValuation), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Profit / Loss", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = String.format("%+.2f (%.1f%%)", profit, profitPercent),
                                style = MaterialTheme.typography.titleLarge,
                                color = if (profit >= 0) Color.Green else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Invested Capital: ${String.format("$%.2f", investedAmount)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showCapitalDialog = true }, modifier = Modifier.weight(1f)) { Text("Add/Remove Capital") }
                Button(onClick = { showValuationDialog = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)) { Text("Update Value") }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Combined History (Transactions & Snapshots) could be complex. 
            // For now, let's just show Transactions (Capital Movements)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions) { t ->
                    Text("${if(t.isExpense) "Withdrawn" else "Deposited"}: ${t.amount} (${t.date.toFormattedDate()})")
                }
            }
        }
    }
    
    if (showCapitalDialog) {
        AddCapitalDialog(onDismiss = { showCapitalDialog = false }) { amount, isWithdrawal ->
            repository.addTransaction(Transaction(
                walletId = walletId,
                amount = amount,
                isExpense = isWithdrawal, // Expense = Withdrawal
                category = "Investment Capital",
                date = System.currentTimeMillis(),
                description = if (isWithdrawal) "Withdrawal" else "Deposit"
            ))
            refreshData(walletId, repository) { t, s, i, c -> transactions = t; snapshots = s; investedAmount = i; currentValuation = c }
            showCapitalDialog = false
        }
    }
    
    if (showValuationDialog) {
        UpdateValuationDialog(onDismiss = { showValuationDialog = false }) { value ->
            repository.addSnapshot(InvestmentSnapshot(
                walletId = walletId,
                date = System.currentTimeMillis(),
                totalValue = value,
                investedAmount = repository.getWalletBalance(walletId) // Capture current invested capital at this moment
            ))
            refreshData(walletId, repository) { t, s, i, c -> transactions = t; snapshots = s; investedAmount = i; currentValuation = c }
            showValuationDialog = false
        }
    }
}

private fun Long.toFormattedDate(): String = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(this))

private fun refreshData(
    walletId: Long, 
    repo: FinanceRepository, 
    callback: (List<Transaction>, List<InvestmentSnapshot>, Double, Double) -> Unit
) {
    val t = repo.getTransactionsForWallet(walletId)
    val s = repo.getSnapshots(walletId)
    val i = repo.getWalletBalance(walletId)
    val c = if(s.isNotEmpty()) s.last().totalValue else i
    callback(t, s, i, c)
}

@Composable
fun AddCapitalDialog(onDismiss: () -> Unit, onSave: (Double, Boolean) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var isWithdrawal by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage Capital", style = MaterialTheme.typography.headlineSmall)
                Row {
                    RadioButton(selected = !isWithdrawal, onClick = { isWithdrawal = false })
                    Text("Deposit", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = isWithdrawal, onClick = { isWithdrawal = true })
                    Text("Withdraw", modifier = Modifier.align(Alignment.CenterVertically))
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                Button(onClick = { onSave(amount.toDoubleOrNull() ?: 0.0, isWithdrawal) }, modifier = Modifier.fillMaxWidth().padding(top=16.dp)) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun UpdateValuationDialog(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var value by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Update Current Value", style = MaterialTheme.typography.headlineSmall)
                Text("Enter the total current value of your portfolio.")
                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Total Value") })
                Button(onClick = { onSave(value.toDoubleOrNull() ?: 0.0) }, modifier = Modifier.fillMaxWidth().padding(top=16.dp)) {
                    Text("Update")
                }
            }
        }
    }
}
