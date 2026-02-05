package com.example.androidpersonal.data.finance

data class InvestmentSnapshot(
    val id: Long = -1,
    val walletId: Long,
    val date: Long,
    val totalValue: Double, // User entered current value
    val investedAmount: Double // Calculated system value (net deposits) at that time
)
