package com.example.androidpersonal.data.finance

enum class WalletType {
    NORMAL,
    INVESTMENT
}

data class Wallet(
    val id: Long = -1,
    val name: String,
    val type: WalletType,
    val creationDate: Long = System.currentTimeMillis()
)
