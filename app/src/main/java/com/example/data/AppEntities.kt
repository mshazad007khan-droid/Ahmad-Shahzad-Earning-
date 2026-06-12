package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earning_plans")
data class EarningPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val cost: Double,
    val dailyEarning: Double,
    val validityDays: Int
)

@Entity(tableName = "user_balance")
data class UserBalance(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 0.0,
    val totalDeposit: Double = 0.0,
    val totalWithdraw: Double = 0.0,
    val easypaisaNumber: String = "03123456789", // Default Admin Easypaisa Number
    val easypaisaName: String = "Ahmad Shahzad"  // Default Admin Easypaisa Name
)

@Entity(tableName = "user_plans")
data class UserPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int,
    val title: String,
    val dailyEarning: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val lastClaimDate: Long = 0L // Last time earnings were claimed (Epoch milliseconds)
)

@Entity(tableName = "transaction_records")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DEPOSIT" or "WITHDRAWAL"
    val amount: Double,
    val paymentMethod: String = "Easypaisa",
    val accountNumber: String,
    val accountName: String,
    val referenceId: String = "", // TRX Transaction ID (important for Deposits)
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
