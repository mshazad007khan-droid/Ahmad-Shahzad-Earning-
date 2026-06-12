package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.EarningPlan
import com.example.data.TransactionRecord
import com.example.data.UserBalance
import com.example.data.UserPlan
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    Home,            // User Dashboard
    EarnPlans,       // Shop for investment plans
    Deposit,         // User deposit screen (via Easypaisa)
    Withdraw,        // User withdrawal screen
    AdminDashboard  // Administrative Dashboard
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    // Expose flows to the Compose UI
    val userBalance: StateFlow<UserBalance?>
    val earningPlans: StateFlow<List<EarningPlan>>
    val userPlans: StateFlow<List<UserPlan>>
    val transactions: StateFlow<List<TransactionRecord>>

    // Application overall Navigation state
    var currentScreen by mutableStateOf(AppScreen.Home)
        private set

    // Shared Flow for snackbar notifications
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.earningDao())

        // Create flows from repository
        userBalance = repository.userBalance.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        earningPlans = repository.allEarningPlans.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userPlans = repository.userPlans.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data & initialize account
        viewModelScope.launch {
            repository.ensureUserBalanceInitialized()
            repository.seedDefaultPlans()
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
    }

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }

    // Purchase Plan
    fun buyPlan(plan: EarningPlan) {
        viewModelScope.launch {
            val success = repository.buyPlan(plan)
            if (success) {
                showToast("🎉 Purchased plan: ${plan.title} successfully!")
            } else {
                showToast("❌ Insufficient balance to buy ${plan.title}! Current Plan cost is PKR ${plan.cost}")
            }
        }
    }

    // Daily Earnings Claim Click
    fun claimDailyEarnings() {
        viewModelScope.launch {
            val totalClaimed = repository.claimDailyEarnings()
            if (totalClaimed > 0.0) {
                showToast("✅ Successfully claimed PKR $totalClaimed daily income from active plans!")
            } else {
                showToast("ℹ️ Next claiming is available 24 hours after your last activation / purchase.")
            }
        }
    }

    // Submit Deposit request
    fun submitDeposit(amount: Double, accountNumber: String, accountName: String, trxId: String) {
        if (amount < 200.0) {
            showToast("❌ Minimum deposit amount is PKR 200.")
            return
        }
        if (accountNumber.isEmpty() || accountName.isEmpty() || trxId.isEmpty()) {
            showToast("❌ Please fill in all the payment fields correctly.")
            return
        }
        viewModelScope.launch {
            repository.requestDeposit(amount, accountNumber, accountName, trxId)
            showToast("📥 Deposit request of PKR $amount sent to admin! (TRX ID: $trxId)")
            navigateTo(AppScreen.Home)
        }
    }

    // Submit Withdrawal Request
    fun submitWithdrawal(amount: Double, accountNumber: String, accountName: String) {
        if (amount < 500.0) {
            showToast("❌ Minimum withdrawal amount is PKR 500.")
            return
        }
        if (accountNumber.isEmpty() || accountName.isEmpty()) {
            showToast("❌ Please fill in withdrawal account info.")
            return
        }
        viewModelScope.launch {
            val success = repository.requestWithdrawal(amount, accountNumber, accountName)
            if (success) {
                showToast("📤 Withdrawal request of PKR $amount has been submitted to admin.")
                navigateTo(AppScreen.Home)
            } else {
                showToast("❌ Insufficient balance for this withdrawal request.")
            }
        }
    }

    // ================= ADMIN FUNCTIONS =================

    // Add Plan (Admin)
    fun addEarningPlan(title: String, cost: Double, dailyEarning: Double, validityDays: Int) {
        if (title.isEmpty() || cost <= 0.0 || dailyEarning <= 0.0 || validityDays <= 0) {
            showToast("❌ Invalid plan parameters.")
            return
        }
        viewModelScope.launch {
            val newPlan = EarningPlan(
                title = title,
                cost = cost,
                dailyEarning = dailyEarning,
                validityDays = validityDays
            )
            repository.insertEarningPlan(newPlan)
            showToast("➕ Successfully added new plan: $title")
        }
    }

    // Edit Plan (Admin)
    fun updateEarningPlan(id: Int, title: String, cost: Double, dailyEarning: Double, validityDays: Int) {
        if (title.isEmpty() || cost <= 0.0 || dailyEarning <= 0.0 || validityDays <= 0) {
            showToast("❌ Invalid parameters to update plan.")
            return
        }
        viewModelScope.launch {
            val updatedPlan = EarningPlan(
                id = id,
                title = title,
                cost = cost,
                dailyEarning = dailyEarning,
                validityDays = validityDays
            )
            repository.updateEarningPlan(updatedPlan)
            showToast("📝 Successfully updated plan: $title")
        }
    }

    // Delete Plan (Admin)
    fun deleteEarningPlan(plan: EarningPlan) {
        viewModelScope.launch {
            repository.deleteEarningPlan(plan)
            showToast("🗑️ Deleted plan: ${plan.title}")
        }
    }

    // Approve Deposit / Withdrawal
    fun approveTransaction(recordId: Int) {
        viewModelScope.launch {
            val success = repository.approveTransaction(recordId)
            if (success) {
                showToast("✓ Approved request! User balance and logs synchronized.")
            } else {
                showToast("❌ Failed to approve transaction.")
            }
        }
    }

    // Reject Deposit / Withdrawal
    fun rejectTransaction(recordId: Int) {
        viewModelScope.launch {
            val success = repository.rejectTransaction(recordId)
            if (success) {
                showToast("✕ Rejected request and refunded user if needed.")
            } else {
                showToast("❌ Failed to reject transaction.")
            }
        }
    }

    // Update Easypaisa receiver credentials (Admin)
    fun updateEasypaisaSettings(number: String, name: String) {
        if (number.length < 10 || name.isEmpty()) {
            showToast("❌ Please enter correct Easypaisa credentials.")
            return
        }
        viewModelScope.launch {
            repository.updateEasypaisaDetails(number, name)
            showToast("⚙️ Easypaisa deposit details updated.")
        }
    }
}
