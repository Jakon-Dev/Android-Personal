package com.example.androidpersonal.data.finance

data class Debt(
    val id: Long = -1,
    val transactionId: Long, // Link to the original expense
    val debtor: String, // Who owns the money
    val creditor: String, // Who is owed the money
    val amount: Double,
    val isSettled: Boolean = false
)
