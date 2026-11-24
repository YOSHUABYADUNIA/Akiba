package com.akiba.app.data.local

import androidx.room.*
import com.akiba.app.data.model.Member
import com.akiba.app.data.model.TontineGroup
import com.akiba.app.data.model.TontineTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AkibaDao {
    @Query("SELECT * FROM tontine_groups ORDER BY updatedAt DESC")
    fun getAllGroups(): Flow<List<TontineGroup>>

    @Query("SELECT * FROM tontine_groups WHERE id = :groupId")
    fun getGroupById(groupId: String): Flow<TontineGroup?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TontineGroup)

    @Query("SELECT * FROM tontine_groups WHERE isSynced = 0")
    suspend fun getUnsyncedGroups(): List<TontineGroup>

    @Query("SELECT * FROM members WHERE groupId = :groupId AND isActive = 1 ORDER BY payoutOrder ASC")
    fun getMembersByGroup(groupId: String): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Query("SELECT * FROM members WHERE isSynced = 0")
    suspend fun getUnsyncedMembers(): List<Member>

    @Query("SELECT * FROM transactions WHERE groupId = :groupId ORDER BY transactionDate DESC")
    fun getTransactionsByGroup(groupId: String): Flow<List<TontineTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TontineTransaction)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TontineTransaction>
}
