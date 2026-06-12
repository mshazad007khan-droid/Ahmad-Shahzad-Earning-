package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val earningDao: EarningDao) {

    // Earning Plans
    val allEarningPlans: Flow<List<EarningPlan>> = earningDao.getAllEarningPlans()

    suspend fun insertEarningPlan(plan: EarningPlan) {
        earningDao.insertEarningPlan(plan)
    }

    suspend fun updateEarningPlan(plan: EarningPlan) {
        earningDao.updateEarningPlan(plan)
    }

    suspend fun deleteEarningPlan(plan: EarningPlan) {
        earningDao.deleteEarningPlan(plan)
    }

    suspend fun getEarningPlanById(id: Int): EarningPlan? {
        return earningDao.getEarningPlanById(id)
    }

    // User Balance
    val userBalance: Flow<UserBalance?> = earningDao.getUserBalanceFlow()

    suspend fun insertUserBalance(balance: UserBalance) {
        earningDao.insertUserBalance(balance)
    }

    suspend fun ensureUserBalanceInitialized() {
        val current = earningDao.getUserBalance()
        if (current == null) {
            earningDao.insertUserBalance(UserBalance())
        }
    }

    // User Purchased Plans
    val userPlans: Flow<List<UserPlan>> = earningDao.getUserPlansFlow()

    suspend fun buyPlan(plan: EarningPlan): Boolean {
        val currentBalanceObj = earningDao.getUserBalance() ?: UserBalance()
        if (currentBalanceObj.balance >= plan.cost) {
            // Deduct cost and add purchased plan
            val updatedBalance = currentBalanceObj.copy(
                balance = currentBalanceObj.balance - plan.cost
            )
            earningDao.insertUserBalance(updatedBalance)
            
            val userPlan = UserPlan(
                planId = plan.id,
                title = plan.title,
                dailyEarning = plan.dailyEarning,
                purchaseDate = System.currentTimeMillis(),
                lastClaimDate = 0L // Not claimed yet
            )
            earningDao.insertUserPlan(userPlan)
            return true
        }
        return false
    }

    suspend fun claimDailyEarnings(): Double {
        val activePlans = earningDao.getUserPlans()
        var totalClaimed = 0.0
        val now = System.currentTimeMillis()
        
        activePlans.forEach { userPlan ->
            // Check if already claimed today (within last 24 hours, or calendar day)
            // Cooldown of 24 hours is most robust and easiest to track
            if (now - userPlan.lastClaimDate >= 24 * 60 * 60 * 1000) {
                totalClaimed += userPlan.dailyEarning
                val updatedPlan = userPlan.copy(lastClaimDate = now)
                earningDao.updateUserPlan(updatedPlan)
            }
        }

        if (totalClaimed > 0.0) {
            val balanceObj = earningDao.getUserBalance() ?: UserBalance()
            val updatedBalance = balanceObj.copy(
                balance = balanceObj.balance + totalClaimed
            )
            earningDao.insertUserBalance(updatedBalance)
        }
        return totalClaimed
    }

    // Transactions
    val allTransactions: Flow<List<TransactionRecord>> = earningDao.getAllTransactionsFlow()

    suspend fun requestDeposit(amount: Double, accountNumber: String, accountName: String, trxId: String) {
        val record = TransactionRecord(
            type = "DEPOSIT",
            amount = amount,
            paymentMethod = "Easypaisa",
            accountNumber = accountNumber,
            accountName = accountName,
            referenceId = trxId,
            status = "PENDING"
        )
        earningDao.insertTransactionRecord(record)
    }

    suspend fun requestWithdrawal(amount: Double, accountNumber: String, accountName: String): Boolean {
        val balanceObj = earningDao.getUserBalance() ?: UserBalance()
        if (balanceObj.balance >= amount) {
            // Deduct from balance immediately on request to prevent double spending
            val updatedBalance = balanceObj.copy(
                balance = balanceObj.balance - amount
            )
            earningDao.insertUserBalance(updatedBalance)

            val record = TransactionRecord(
                type = "WITHDRAWAL",
                amount = amount,
                paymentMethod = "Easypaisa",
                accountNumber = accountNumber,
                accountName = accountName,
                referenceId = "",
                status = "PENDING"
            )
            earningDao.insertTransactionRecord(record)
            return true
        }
        return false
    }

    suspend fun approveTransaction(recordId: Int): Boolean {
        val tx = earningDao.getTransactionById(recordId) ?: return false
        if (tx.status != "PENDING") return false

        val updatedTx = tx.copy(status = "APPROVED")
        earningDao.updateTransactionRecord(updatedTx)

        val balanceObj = earningDao.getUserBalance() ?: UserBalance()
        if (tx.type == "DEPOSIT") {
            // Add deposit to balance
            val updatedBalance = balanceObj.copy(
                balance = balanceObj.balance + tx.amount,
                totalDeposit = balanceObj.totalDeposit + tx.amount
            )
            earningDao.insertUserBalance(updatedBalance)
        } else if (tx.type == "WITHDRAWAL") {
            // Withdrawal was already deducted, just add to total withdrawal metric
            val updatedBalance = balanceObj.copy(
                totalWithdraw = balanceObj.totalWithdraw + tx.amount
            )
            earningDao.insertUserBalance(updatedBalance)
        }
        return true
    }

    suspend fun rejectTransaction(recordId: Int): Boolean {
        val tx = earningDao.getTransactionById(recordId) ?: return false
        if (tx.status != "PENDING") return false

        val updatedTx = tx.copy(status = "REJECTED")
        earningDao.updateTransactionRecord(updatedTx)

        val balanceObj = earningDao.getUserBalance() ?: UserBalance()
        if (tx.type == "WITHDRAWAL") {
            // Refund the user since withdrawal was rejected
            val updatedBalance = balanceObj.copy(
                balance = balanceObj.balance + tx.amount
            )
            earningDao.insertUserBalance(updatedBalance)
        }
        return true
    }

    suspend fun updateEasypaisaDetails(number: String, name: String) {
        val balanceObj = earningDao.getUserBalance() ?: UserBalance()
        val updated = balanceObj.copy(
            easypaisaNumber = number,
            easypaisaName = name
        )
        earningDao.insertUserBalance(updated)
    }

    suspend fun seedDefaultPlans() {
        val currentPlans = earningDao.getAllEarningPlans().firstOrNull() ?: emptyList()
        if (currentPlans.isEmpty()) {
            val defaults = listOf(
                EarningPlan(title = "VIP Plan 1 (Starter)", cost = 1000.0, dailyEarning = 100.0, validityDays = 30),
                EarningPlan(title = "VIP Plan 2 (Silver)", cost = 3000.0, dailyEarning = 350.0, validityDays = 30),
                EarningPlan(title = "VIP Plan 3 (Gold)", cost = 6000.0, dailyEarning = 800.0, validityDays = 30),
                EarningPlan(title = "VIP Plan 4 (Platinum)", cost = 12000.0, dailyEarning = 1800.0, validityDays = 30),
                EarningPlan(title = "VIP Plan 5 (Diamond)", cost = 25000.0, dailyEarning = 4000.0, validityDays = 30),
                EarningPlan(title = "VIP Plan 6 (Ahmad Shahzad Crown)", cost = 50000.0, dailyEarning = 9000.0, validityDays = 30)
            )
            defaults.forEach { earningDao.insertEarningPlan(it) }
        }
    }
}
