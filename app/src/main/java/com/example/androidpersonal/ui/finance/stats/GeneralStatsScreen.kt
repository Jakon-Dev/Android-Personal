package com.example.androidpersonal.ui.finance.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidpersonal.data.finance.FinanceRepository
import com.example.androidpersonal.data.finance.Transaction
import com.example.androidpersonal.ui.theme.OrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralStatsScreen(
    repository: FinanceRepository,
    onBackClick: () -> Unit
) {
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var totalSpend by remember { mutableDoubleStateOf(0.0) }
    var categorySpend by remember { mutableStateOf(mapOf<String, Double>()) }

    LaunchedEffect(Unit) {
        // Fetch ALL transactions for stats
        // Currently getAllTransactions() in repo might need to be exposed better or we use a separate method
        // Actually, I added getAllTransactions to repo earlier.
        // Let's assume there is a getAllTransactions() that returns EVERYTHING across all wallets (or normal wallets).
        // Usually stats are for Normal spending. Investment withdrawal/deposit shouldn't count as "Category Spend".
        
        // Let's filter client side for better control right now.
        val all = repository.getTransactionsForWallet(1) // FIXME: Need ALL wallets. 
        // Oh right, I need to fetch all wallets, then all transactions for each. 
        // Or add a getAllTransactionsAcrossWallets() in repo.
        // For now, let's iterate wallets.
        
        val wallets = repository.getWallets()
        val allTrans = mutableListOf<Transaction>()
        wallets.forEach { w ->
             // Only include Normal wallets for Spending Stats? 
             // Investment "deposits" are transfers, not expenses usually.
             if (w.type == com.example.androidpersonal.data.finance.WalletType.NORMAL) {
                 allTrans.addAll(repository.getTransactionsForWallet(w.id))
             }
        }
        
        transactions = allTrans
        
        // Calc Stats
        // Filter for Expense only
        val expenses = allTrans.filter { it.isExpense }
        totalSpend = expenses.sumOf { it.amount }
        
        categorySpend = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second } // Sort by amount
            .toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Stats") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Total Spending", style = MaterialTheme.typography.labelMedium)
                    Text(String.format("$%.2f", totalSpend), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                }
            }
            
            Text("By Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categorySpend.toList()) { (category, amount) ->
                    CategoryBar(category, amount, totalSpend)
                }
            }
        }
    }
}

@Composable
fun CategoryBar(category: String, amount: Double, total: Double) {
    val percentage = if (total > 0) (amount / total) else 0.0
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, fontWeight = FontWeight.Bold)
            Text(String.format("$%.2f (%.1f%%)", amount, percentage * 100))
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Bar
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color.LightGray, RoundedCornerShape(4.dp))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.toFloat())
                    .height(8.dp)
                    .background(OrangeAccent, RoundedCornerShape(4.dp))
            )
        }
    }
}
