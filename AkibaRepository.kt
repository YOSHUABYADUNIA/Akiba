package com.akiba.app.data.repository

import com.akiba.app.data.local.AkibaDao
import com.akiba.app.data.model.Member
import com.akiba.app.data.model.TontineGroup
import com.akiba.app.data.model.TontineTransaction
import com.akiba.app.data.remote.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow

class AkibaRepository(private val dao: AkibaDao) {

    fun getAllGroups(): Flow<List<TontineGroup>> = dao.getAllGroups()
    fun getGroupById(id: String): Flow<TontineGroup?> = dao.getGroupById(id)
    fun getMembersByGroup(groupId: String): Flow<List<Member>> = dao.getMembersByGroup(groupId)
    fun getTransactionsByGroup(groupId: String): Flow<List<TontineTransaction>> = dao.getTransactionsByGroup(groupId)

    suspend fun saveGroup(group: TontineGroup) {
        dao.insertGroup(group.copy(isSynced = false, updatedAt = System.currentTimeMillis()))
        syncPendingData()
    }

    suspend fun saveMember(member: Member) {
        dao.insertMember(member.copy(isSynced = false))
        syncPendingData()
    }

    suspend fun saveTransaction(transaction: TontineTransaction) {
        dao.insertTransaction(transaction.copy(isSynced = false))
        syncPendingData()
    }

    suspend fun syncPendingData() {
        try {
            val unsyncedGroups = dao.getUnsyncedGroups()
            for (group in unsyncedGroups) {
                SupabaseConfig.client.from("tontine_groups").upsert(group)
                dao.insertGroup(group.copy(isSynced = true))
            }

            val unsyncedMembers = dao.getUnsyncedMembers()
            for (member in unsyncedMembers) {
                SupabaseConfig.client.from("members").upsert(member)
                dao.insertMember(member.copy(isSynced = true))
            }

            val unsyncedTx = dao.getUnsyncedTransactions()
            for (tx in unsyncedTx) {
                SupabaseConfig.client.from("transactions").upsert(tx)
                dao.insertTransaction(tx.copy(isSynced = true))
            }
        } catch (_: Exception) {
            // Hors-ligne : les données restent locales et seront synchronisées plus tard
        }
    }
}
