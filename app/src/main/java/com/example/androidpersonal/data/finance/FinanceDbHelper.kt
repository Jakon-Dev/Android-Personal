package com.example.androidpersonal.data.finance

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class FinanceDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "finance.db"
        const val DATABASE_VERSION = 2

        // Tables
        const val TABLE_TRANSACTIONS = "transactions"
        const val TABLE_WALLETS = "wallets"
        const val TABLE_DEBTS = "debts"
        const val TABLE_INVESTMENT_SNAPSHOTS = "investment_snapshots"

        // Wallets Columns
        const val COL_WALLET_ID = "id"
        const val COL_WALLET_NAME = "name"
        const val COL_WALLET_TYPE = "type"
        const val COL_WALLET_CREATION = "creation_date"

        // Transactions Columns
        const val COL_TRANS_ID = "id"
        const val COL_TRANS_WALLET_ID = "wallet_id" // FK
        const val COL_TRANS_AMOUNT = "amount"
        const val COL_TRANS_IS_EXPENSE = "is_expense"
        const val COL_TRANS_CATEGORY = "category"
        const val COL_TRANS_DATE = "date"
        const val COL_TRANS_DESC = "description"

        // Debts Columns
        const val COL_DEBT_ID = "id"
        const val COL_DEBT_TRANS_ID = "transaction_id" // FK
        const val COL_DEBT_DEBTOR = "debtor"
        const val COL_DEBT_CREDITOR = "creditor"
        const val COL_DEBT_AMOUNT = "amount"
        const val COL_DEBT_IS_SETTLED = "is_settled"

        // Investment Snapshots Columns
        const val COL_SNAPSHOT_ID = "id"
        const val COL_SNAPSHOT_WALLET_ID = "wallet_id" // FK
        const val COL_SNAPSHOT_DATE = "date"
        const val COL_SNAPSHOT_TOTAL_VALUE = "total_value"
        const val COL_SNAPSHOT_INVESTED = "invested_amount"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createWalletsTable(db)
        createTransactionsTable(db)
        createDebtsTable(db)
        createSnapshotsTable(db)
        
        // Create Default Wallet
        createDefaultWallet(db)
    }

    private fun createWalletsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_WALLETS (
                $COL_WALLET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_WALLET_NAME TEXT,
                $COL_WALLET_TYPE TEXT,
                $COL_WALLET_CREATION INTEGER
            )
        """)
    }
    
    // Updated Transaction Table Creation
    private fun createTransactionsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_TRANS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TRANS_WALLET_ID INTEGER,
                $COL_TRANS_AMOUNT REAL,
                $COL_TRANS_IS_EXPENSE INTEGER,
                $COL_TRANS_CATEGORY TEXT,
                $COL_TRANS_DATE INTEGER,
                $COL_TRANS_DESC TEXT,
                FOREIGN KEY($COL_TRANS_WALLET_ID) REFERENCES $TABLE_WALLETS($COL_WALLET_ID)
            )
        """)
    }
    
    private fun createDebtsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_DEBTS (
                $COL_DEBT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_DEBT_TRANS_ID INTEGER,
                $COL_DEBT_DEBTOR TEXT,
                $COL_DEBT_CREDITOR TEXT,
                $COL_DEBT_AMOUNT REAL,
                $COL_DEBT_IS_SETTLED INTEGER,
                FOREIGN KEY($COL_DEBT_TRANS_ID) REFERENCES $TABLE_TRANSACTIONS($COL_TRANS_ID)
            )
        """)
    }
    
    private fun createSnapshotsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_INVESTMENT_SNAPSHOTS (
                $COL_SNAPSHOT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SNAPSHOT_WALLET_ID INTEGER,
                $COL_SNAPSHOT_DATE INTEGER,
                $COL_SNAPSHOT_TOTAL_VALUE REAL,
                $COL_SNAPSHOT_INVESTED REAL,
                FOREIGN KEY($COL_SNAPSHOT_WALLET_ID) REFERENCES $TABLE_WALLETS($COL_WALLET_ID)
            )
        """)
    }

    private fun createDefaultWallet(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COL_WALLET_NAME, "Main Wallet")
            put(COL_WALLET_TYPE, WalletType.NORMAL.name)
            put(COL_WALLET_CREATION, System.currentTimeMillis())
        }
        db.insert(TABLE_WALLETS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Upgrade Logic for v2
            
            // 1. Create new support tables
            createWalletsTable(db)
            createDebtsTable(db)
            createSnapshotsTable(db)
            
            // 2. Create a default wallet to migrate existing transactions to
            val walletId = 1L
            val walletValues = ContentValues().apply {
                put(COL_WALLET_ID, walletId)
                put(COL_WALLET_NAME, "Main Wallet")
                put(COL_WALLET_TYPE, WalletType.NORMAL.name)
                put(COL_WALLET_CREATION, System.currentTimeMillis())
            }
            db.insert(TABLE_WALLETS, null, walletValues)
            
            // 3. Alter transactions table to add wallet_id
            db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COL_TRANS_WALLET_ID INTEGER DEFAULT $walletId")
        }
    }
    
    // --- CRUD Operations ---
    
    // Wallets
    fun addWallet(name: String, type: WalletType): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_WALLET_NAME, name)
            put(COL_WALLET_TYPE, type.name)
            put(COL_WALLET_CREATION, System.currentTimeMillis())
        }
        val id = db.insert(TABLE_WALLETS, null, values)
        db.close()
        return id
    }
    
    fun getWallets(): List<Wallet> {
        val list = mutableListOf<Wallet>()
        val db = readableDatabase
        val cursor = db.query(TABLE_WALLETS, null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(Wallet(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WALLET_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_WALLET_NAME)),
                    type = WalletType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(COL_WALLET_TYPE))),
                    creationDate = cursor.getLong(cursor.getColumnIndexOrThrow(COL_WALLET_CREATION))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // Transactions
    fun addTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TRANS_WALLET_ID, transaction.walletId)
            put(COL_TRANS_AMOUNT, transaction.amount)
            put(COL_TRANS_IS_EXPENSE, if (transaction.isExpense) 1 else 0)
            put(COL_TRANS_CATEGORY, transaction.category)
            put(COL_TRANS_DATE, transaction.date)
            put(COL_TRANS_DESC, transaction.description)
        }
        val id = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return id
    }

    fun getTransactionsForWallet(walletId: Long): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS, 
            null, 
            "$COL_TRANS_WALLET_ID = ?", 
            arrayOf(walletId.toString()), 
            null, null, 
            "$COL_TRANS_DATE DESC"
        )
        
        if (cursor.moveToFirst()) {
            do {
                list.add(Transaction(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_ID)),
                    walletId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_WALLET_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT)),
                    isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_IS_EXPENSE)) == 1,
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_DATE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESC))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
    
    fun getAllTransactions(): List<Transaction> {
        // Needed for aggregate stats
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, "$COL_TRANS_DATE DESC")
        if (cursor.moveToFirst()) {
            do {
                list.add(Transaction(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_ID)),
                    walletId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_WALLET_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT)),
                    isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_IS_EXPENSE)) == 1,
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRANS_DATE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESC))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // Debts
    fun addDebt(debt: Debt): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_DEBT_TRANS_ID, debt.transactionId)
            put(COL_DEBT_DEBTOR, debt.debtor)
            put(COL_DEBT_CREDITOR, debt.creditor)
            put(COL_DEBT_AMOUNT, debt.amount)
            put(COL_DEBT_IS_SETTLED, if (debt.isSettled) 1 else 0)
        }
        val id = db.insert(TABLE_DEBTS, null, values)
        db.close()
        return id
    }
    
    fun getDebtsForWallet(walletId: Long): List<Debt> {
         // This requires join or simpler separation. For "Normal Wallet", we assume debts are associated
         // with transactions IN that wallet.
         // Query: Select Debts where Debt.transaction_id -> Transaction.wallet_id == walletId
         val query = """
             SELECT d.* FROM $TABLE_DEBTS d
             INNER JOIN $TABLE_TRANSACTIONS t ON d.$COL_DEBT_TRANS_ID = t.$COL_TRANS_ID
             WHERE t.$COL_TRANS_WALLET_ID = ?
         """
         val list = mutableListOf<Debt>()
         val db = readableDatabase
         val cursor = db.rawQuery(query, arrayOf(walletId.toString()))
         
         if (cursor.moveToFirst()) {
            do {
                list.add(Debt(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DEBT_ID)),
                    transactionId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_DEBT_TRANS_ID)),
                    debtor = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEBT_DEBTOR)),
                    creditor = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEBT_CREDITOR)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DEBT_AMOUNT)),
                    isSettled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DEBT_IS_SETTLED)) == 1
                ))
            } while (cursor.moveToNext())
         }
         cursor.close()
         db.close()
         return list
    }
    
    fun settleDebt(debtId: Long) {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_DEBT_IS_SETTLED, 1) }
        db.update(TABLE_DEBTS, values, "$COL_DEBT_ID = ?", arrayOf(debtId.toString()))
        db.close()
    }

    // Snapshots
    fun addSnapshot(snapshot: InvestmentSnapshot): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SNAPSHOT_WALLET_ID, snapshot.walletId)
            put(COL_SNAPSHOT_DATE, snapshot.date)
            put(COL_SNAPSHOT_TOTAL_VALUE, snapshot.totalValue)
            put(COL_SNAPSHOT_INVESTED, snapshot.investedAmount)
        }
        val id = db.insert(TABLE_INVESTMENT_SNAPSHOTS, null, values)
        db.close()
        return id
    }
    
    fun getSnapshots(walletId: Long): List<InvestmentSnapshot> {
        val list = mutableListOf<InvestmentSnapshot>()
        val db = readableDatabase
        val cursor = db.query(TABLE_INVESTMENT_SNAPSHOTS, null, "$COL_SNAPSHOT_WALLET_ID = ?", arrayOf(walletId.toString()), null, null, "$COL_SNAPSHOT_DATE ASC")
        if (cursor.moveToFirst()) {
            do {
                list.add(InvestmentSnapshot(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SNAPSHOT_ID)),
                    walletId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SNAPSHOT_WALLET_ID)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SNAPSHOT_DATE)),
                    totalValue = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SNAPSHOT_TOTAL_VALUE)),
                    investedAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SNAPSHOT_INVESTED))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
