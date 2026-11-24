package com.akiba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akiba.app.data.model.Member
import com.akiba.app.data.model.TontineGroup
import com.akiba.app.data.model.TontineTransaction
import com.akiba.app.data.repository.AkibaRepository
import com.akiba.app.utils.ReceiptHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGroupsScreen(
    repository: AkibaRepository,
    onOpenGroup: (String) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val groups by repository.getAllGroups().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akiba • Mes Tontines", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { scope.launch { repository.syncPendingData() } }) {
                        Icon(Icons.Default.Sync, contentDescription = "Synchroniser")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateGroupClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nouvelle tontine") }
            )
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aucune tontine enregistrée.\nAppuyez sur '+ Nouvelle tontine' pour démarrer votre Ikibiri ou Likilemba.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groups) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenGroup(group.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Badge { Text("Tour #${group.currentCycle}") }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Cotisation : ${String.format("%,.0f", group.contributionAmount)} ${group.currency} (${group.frequency})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (group.isSynced) "☁️ Synchronisé" else "📱 Enregistré hors-ligne",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    repository: AkibaRepository,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("BIF") }
    var amountText by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("weekly") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer une tontine") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du groupe (ex: Ikibiri Musenyi)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it.uppercase() },
                label = { Text("Devise (BIF, CDF, USD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Montant par cotisation") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("daily" to "Jour", "weekly" to "Semaine", "monthly" to "Mois").forEach { (key, label) ->
                    FilterChip(
                        selected = frequency == key,
                        onClick = { frequency = key },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        scope.launch {
                            repository.saveGroup(
                                TontineGroup(
                                    name = name.trim(),
                                    currency = currency.trim().ifEmpty { "BIF" },
                                    contributionAmount = amount,
                                    frequency = frequency
                                )
                            )
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Enregistrer la tontine")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDashboardScreen(
    groupId: String,
    repository: AkibaRepository,
    onBack: () -> Unit
) {
    val group by repository.getGroupById(groupId).collectAsState(initial = null)
    val members by repository.getMembersByGroup(groupId).collectAsState(initial = emptyList())
    val transactions by repository.getTransactionsByGroup(groupId).collectAsState(initial = emptyList())

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedMemberForPayment by remember { mutableStateOf<Member?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentGroup = group ?: return
    val cycleTransactions = transactions.filter { it.cycleNumber == currentGroup.currentCycle && it.type == "contribution" }
    val totalCollected = cycleTransactions.sumOf { it.amount }
    val expectedTotal = members.size * currentGroup.contributionAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGroup.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Retour") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddMemberDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Membre") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tour actuel : #${currentGroup.currentCycle}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Caisse : ${String.format("%,.0f", totalCollected)} / ${String.format("%,.0f", expectedTotal)} ${currentGroup.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    repository.saveGroup(currentGroup.copy(currentCycle = currentGroup.currentCycle + 1))
                                }
                            }
                        ) {
                            Text("Passer au tour suivant (#${currentGroup.currentCycle + 1})")
                        }
                    }
                }
            }

            item {
                Text("Membres (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(members) { member ->
                val hasPaidThisCycle = cycleTransactions.any { it.memberId == member.id }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("#${member.payoutOrder} • ${member.fullName}", fontWeight = FontWeight.Bold)
                            if (member.phone.isNotBlank()) {
                                Text(member.phone, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = if (hasPaidThisCycle) "✅ A cotisé ce tour" else "⏳ En attente",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        if (!hasPaidThisCycle) {
                            Button(onClick = { selectedMemberForPayment = member }) {
                                Text("Payer")
                            }
                        } else {
                            IconButton(onClick = {
                                val receipt = ReceiptHelper.formatReceiptText(
                                    groupName = currentGroup.name,
                                    memberName = member.fullName,
                                    amount = currentGroup.contributionAmount,
                                    currency = currentGroup.currency,
                                    cycleNumber = currentGroup.currentCycle,
                                    paymentMethod = "cash"
                                )
                                ReceiptHelper.shareReceipt(context, receipt)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Partager reçu")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMemberDialog) {
        var memberName by remember { mutableStateOf("") }
        var memberPhone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Ajouter un membre") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = memberName, onValueChange = { memberName = it }, label = { Text("Nom complet") })
                    OutlinedTextField(value = memberPhone, onValueChange = { memberPhone = it }, label = { Text("Téléphone (optionnel)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (memberName.isNotBlank()) {
                        scope.launch {
                            repository.saveMember(
                                Member(
                                    groupId = currentGroup.id,
                                    fullName = memberName.trim(),
                                    phone = memberPhone.trim(),
                                    payoutOrder = members.size + 1
                                )
                            )
                            showAddMemberDialog = false
                        }
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) { Text("Annuler") }
            }
        )
    }

    selectedMemberForPayment?.let { member ->
        var method by remember { mutableStateOf("cash") }
        AlertDialog(
            onDismissRequest = { selectedMemberForPayment = null },
            title = { Text("Cotisation de ${member.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Montant : ${String.format("%,.0f", currentGroup.contributionAmount)} ${currentGroup.currency}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = method == "cash", onClick = { method = "cash" }, label = { Text("Espèces") })
                        FilterChip(selected = method == "mobile_money", onClick = { method = "mobile_money" }, label = { Text("Mobile Money") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        repository.saveTransaction(
                            TontineTransaction(
                                groupId = currentGroup.id,
                                memberId = member.id,
                                cycleNumber = currentGroup.currentCycle,
                                type = "contribution",
                                amount = currentGroup.contributionAmount,
                                paymentMethod = method
                            )
                        )
                        val receipt = ReceiptHelper.formatReceiptText(
                            groupName = currentGroup.name,
                            memberName = member.fullName,
                            amount = currentGroup.contributionAmount,
                            currency = currentGroup.currency,
                            cycleNumber = currentGroup.currentCycle,
                            paymentMethod = method
                        )
                        selectedMemberForPayment = null
                        ReceiptHelper.shareReceipt(context, receipt)
                    }
                }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Valider & Reçu")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForPayment = null }) { Text("Annuler") }
            }
        )
    }
}
