package com.example.androidpersonal.data.finance

import android.content.Context
import java.util.Calendar

class FinanceRepository(context: Context) {
    private val dbHelper = FinanceDbHelper(context)

    // --- Wallets ---
    fun addWallet(name: String, type: WalletType): Long {
        return dbHelper.addWallet(name, type)
    }

    fun getWallets(): List<Wallet> {
        return dbHelper.getWallets()
    }
    
    // --- Transactions ---
    fun addTransaction(transaction: Transaction) {
        dbHelper.addTransaction(transaction)
    }

    fun getTransactionsForWallet(walletId: Long): List<Transaction> {
        return dbHelper.getTransactionsForWallet(walletId)
    }
    
    fun deleteTransaction(id: Long) {
       // dbHelper.deleteTransaction(id) // Needs implementation in DbHelper if we want this feature back fully
    }
    
    // --- Debts ---
    fun addDebt(debt: Debt) {
        dbHelper.addDebt(debt)
    }
    
    fun getDebtsForWallet(walletId: Long): List<Debt> {
        return dbHelper.getDebtsForWallet(walletId)
    }
    
    fun settleDebt(debtId: Long) {
        dbHelper.settleDebt(debtId)
    }
    
    // --- Investment Snapshots ---
    fun addSnapshot(snapshot: InvestmentSnapshot) {
        dbHelper.addSnapshot(snapshot)
    }
    
    fun getSnapshots(walletId: Long): List<InvestmentSnapshot> {
        return dbHelper.getSnapshots(walletId)
    }

    // --- Stats & Calculations ---

    fun getWalletBalance(walletId: Long): Double {
        val transactions = getTransactionsForWallet(walletId)
        return transactions.sumOf { 
            if (it.isExpense) -it.amount else it.amount 
        }
    }
    
    fun getTotalNetWorth(): Double {
        // Sum of all normal wallets balance + Latest Investment Snapshot Values
        val wallets = getWallets()
        var total = 0.0
        
        for (wallet in wallets) {
            if (wallet.type == WalletType.NORMAL) {
                total += getWalletBalance(wallet.id)
            } else {
                // For Investment, assume Current Value is latest snapshot OR (invested + profit)
                // Simplest: Latest Snapshot Value. If no snapshot, use net deposits.
                val snapshots = getSnapshots(wallet.id)
                if (snapshots.isNotEmpty()) {
                    total += snapshots.last().totalValue
                } else {
                    total += getWalletBalance(wallet.id) // Just Add/Remove capital
                }
            }
        }
        return total
    }

    fun getMonthlySpend(walletId: Long? = null): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val transactions = if (walletId != null) getTransactionsForWallet(walletId) else dbHelper.getAllTransactions()
        
        return transactions
            .filter { 
                it.isExpense && 
                it.category != "Investment" // Exclude transfers if needed
            } 
            .filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
    }
    
    fun getInvestmentPerformance(walletId: Long): Pair<Double, Double> {
        // Returns (ProfitAmount, ProfitPercent)
        val snapshots = getSnapshots(walletId)
        if (snapshots.isEmpty()) return Pair(0.0, 0.0)
        
        // Latest check
        val latest = snapshots.last()
        val profit = latest.totalValue - latest.investedAmount
        val percent = if (latest.investedAmount != 0.0) (profit / latest.investedAmount) * 100 else 0.0
        
        return Pair(profit, percent)
    }
}
