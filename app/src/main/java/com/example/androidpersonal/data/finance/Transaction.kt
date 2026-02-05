package com.example.androidpersonal.data.finance

@Suppress("EnumEntryName")
enum class TransactionType {
    INCOME,
    EXPENSE,
    DEBT_SETTLEMENT, // Special type for paying off a debt
    INVESTMENT_DEPOSIT,
    INVESTMENT_WITHDRAWAL,
    VALUATION_UPDATE // Marker transaction ? Or maybe just rely on snapshots. Sticky.
    // Let's stick to simple INCOME/EXPENSE for Normal, and DEPOSIT/WITHDRAWAL for Investment.
}

data class Transaction(
    val id: Long = -1,
    val walletId: Long,
    val amount: Double,
    val isExpense: Boolean, // True for Expense/Withdrawal, False for Income/Deposit
    val category: String,
    val date: Long, // Timestamp
    val description: String = ""
)
