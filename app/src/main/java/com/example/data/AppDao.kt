package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningDao {
    // ---- Earning Plans ----
    @Query("SELECT * FROM earning_plans ORDER BY cost ASC")
    fun getAllEarningPlans(): Flow<List<EarningPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarningPlan(plan: EarningPlan)

    @Update
    suspend fun updateEarningPlan(plan: EarningPlan)

    @Delete
    suspend fun deleteEarningPlan(plan: EarningPlan)

    @Query("SELECT * FROM earning_plans WHERE id = :id")
    suspend fun getEarningPlanById(id: Int): EarningPlan?

    // ---- User Balance ----
    @Query("SELECT * FROM user_balance WHERE id = 1")
    fun getUserBalanceFlow(): Flow<UserBalance?>

    @Query("SELECT * FROM user_balance WHERE id = 1")
    suspend fun getUserBalance(): UserBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBalance(balance: UserBalance)

    // ---- User Purchased Plans ----
    @Query("SELECT * FROM user_plans ORDER BY purchaseDate DESC")
    fun getUserPlansFlow(): Flow<List<UserPlan>>

    @Query("SELECT * FROM user_plans ORDER BY purchaseDate DESC")
    suspend fun getUserPlans(): List<UserPlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlan(userPlan: UserPlan)

    @Update
    suspend fun updateUserPlan(userPlan: UserPlan)

    @Query("DELETE FROM user_plans WHERE id = :id")
    suspend fun deleteUserPlanById(id: Int)

    // ---- Transactions ----
    @Query("SELECT * FROM transaction_records ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionRecord(record: TransactionRecord)

    @Update
    suspend fun updateTransactionRecord(record: TransactionRecord)

    @Query("SELECT * FROM transaction_records WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionRecord?
}
