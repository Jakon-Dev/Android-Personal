
// Mock Classes
data class Debt(val amount: Double)
data class Transaction(val amount: Double)

// --- Debt Logic Check ---
println("--- Debt Logic Check ---")
val totalAmount = 100.0
val splitWith = listOf("Alice", "Bob")
val peopleCount = splitWith.size + 1 // + Me
val share = totalAmount / peopleCount

println("Total Amount: $totalAmount")
println("People: Me + ${splitWith}")
println("Share per person: $share")

val myExpense = share
val debts = splitWith.map { Debt(share) }

val totalAccounted = myExpense + debts.sumOf { it.amount }
println("My Expense: $myExpense")
println("Debts Created: ${debts.size} x $share = ${debts.sumOf{it.amount}}")
println("Total Accounted: $totalAccounted")

if (Math.abs(totalAccounted - totalAmount) < 0.01) {
    println("SUCCESS: Debt splitting adds up.")
} else {
    println("FAILURE: Math mismatch.")
}

// --- Investment Logic Check ---
println("\n--- Investment Logic Check ---")
val deposits = listOf(1000.0, 500.0) // 1500 invested
val withdrawals = listOf(200.0) // 1300 net invested
val currentValuation = 1400.0

val totalInvested = deposits.sum() - withdrawals.sum()
val profit = currentValuation - totalInvested
val percent = (profit / totalInvested) * 100

println("Deposits: ${deposits.sum()}")
println("Withdrawals: ${withdrawals.sum()}")
println("Net Invested: $totalInvested")
println("Current Value: $currentValuation")
println("Profit: $profit")
println("ROI: $percent%")

if (totalInvested == 1300.0 && profit == 100.0) {
    println("SUCCESS: Investment math correct.")
} else {
    println("FAILURE: Investment math wrong.")
}
