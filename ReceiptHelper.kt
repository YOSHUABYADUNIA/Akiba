package com.akiba.app.utils

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptHelper {

    fun formatReceiptText(
        groupName: String,
        memberName: String,
        amount: Double,
        currency: String,
        cycleNumber: Int,
        paymentMethod: String
    ): String {
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val methodLabel = if (paymentMethod == "mobile_money") "Mobile Money" else "Espèces"

        return """
            ✅ *REÇU AKIBA - TONTINE*
            -------------------------
            📌 Groupe : *$groupName*
            👤 Membre : *$memberName*
            🔄 Tour N° : $cycleNumber
            💰 Montant : *${String.format("%,.0f", amount)} $currency*
            💳 Mode : $methodLabel
            📅 Date : $dateStr
            -------------------------
            _Généré avec Akiba (Ikibiri / Likilemba)_
        """.trimIndent()
    }

    fun shareReceipt(context: Context, receiptText: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, receiptText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Envoyer le reçu via...")
        context.startActivity(shareIntent)
    }
}
