package com.akiba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

@Serializable
@Entity(tableName = "tontine_groups")
data class TontineGroup(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @SerialName("owner_id")
    val ownerId: String? = null,
    val name: String,
    val currency: String = "BIF",
    @SerialName("contribution_amount")
    val contributionAmount: Double,
    val frequency: String = "weekly",
    @SerialName("penalty_amount")
    val penaltyAmount: Double = 0.0,
    @SerialName("current_cycle")
    val currentCycle: Int = 1,
    val status: String = "active",
    @Transient
    val isSynced: Boolean = false,
    @Transient
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "members")
data class Member(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @SerialName("group_id")
    val groupId: String,
    @SerialName("full_name")
    val fullName: String,
    val phone: String = "",
    val role: String = "member",
    @SerialName("payout_order")
    val payoutOrder: Int = 1,
    @SerialName("has_received_payout")
    val hasReceivedPayout: Boolean = false,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @Transient
    val isSynced: Boolean = false
)

@Serializable
@Entity(tableName = "transactions")
data class TontineTransaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @SerialName("group_id")
    val groupId: String,
    @SerialName("member_id")
    val memberId: String,
    @SerialName("cycle_number")
    val cycleNumber: Int,
    val type: String = "contribution",
    val amount: Double,
    @SerialName("payment_method")
    val paymentMethod: String = "cash",
    val status: String = "paid",
    val note: String = "",
    @Transient
    val transactionDate: Long = System.currentTimeMillis(),
    @Transient
    val isSynced: Boolean = false
)
