package com.example.spliteasy.transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Expense
import com.example.spliteasy.api.Groups
import com.example.spliteasy.api.Transaction
import com.example.spliteasy.api.TransactionType
import com.example.spliteasy.api.Transfer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TransactionsScreen(
    groupId: String="",
    onTransactionClick: (id: String) -> Unit = {},
    onAddTransaction: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val db = FirebaseFirestore.getInstance()
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var transfers by remember { mutableStateOf<List<Transfer>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var accountNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTypes by remember { mutableStateOf(setOf<TransactionType>()) }
    var selectedAccounts by remember { mutableStateOf(setOf<String>()) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    val allAccounts = (expenses.flatMap { it.paidBy.mapNotNull { it.account?.id } + it.paidFor.mapNotNull { it.account?.id } } +
            transfers.flatMap { it.from.mapNotNull { it.account?.id } + it.to.mapNotNull { it.account?.id } }).toSet()

    val activeFiltersCount = selectedTypes.size + selectedAccounts.size

    suspend fun loadTransactions(){
        isLoading = true
        val fetchedExpenses = Groups.getExpenses(db, groupId)
        val fetchedTransfers = Groups.getTransfers(db, groupId)
        val fetchedAccounts = Groups.getAccounts(db, groupId) // returns List<Account>

        val nameMap = fetchedAccounts.associate { it.id to it.name }
        accountNames = nameMap

        expenses = fetchedExpenses
        transfers = fetchedTransfers
        transactions = fetchedExpenses.map { Transaction.fromExpense(db, groupId, it) } +
                fetchedTransfers.map { Transaction.fromTransfer(db, groupId, it) }

        isLoading = false
    }

    LaunchedEffect(groupId) {
        loadTransactions()
    }

    val filteredTransactions = transactions.filter {
        (selectedTypes.isEmpty() || selectedTypes.contains(it.type)) &&
                (selectedAccounts.isEmpty() || selectedAccounts.intersect(it.accountIds).isNotEmpty())
    }

    val groupedTransactions = filteredTransactions
        .sortedByDescending { it.createdOn }
        .groupBy { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.createdOn.toDate()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        BadgedBox(
                            badge = {
                                if (activeFiltersCount > 0) {
                                    Badge { Text("$activeFiltersCount") }
                                }
                            }
                        ) {
                            IconButton(onClick = { filterMenuExpanded = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Filter")
                            }
                        }

                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            Text("Transaction Types", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleSmall)
                            TransactionType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = selectedTypes.contains(type),
                                                onCheckedChange = {
                                                    selectedTypes = if (selectedTypes.contains(type))
                                                        selectedTypes - type
                                                    else selectedTypes + type
                                                }
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(type.name)
                                        }
                                    },
                                    onClick = {
                                        selectedTypes = if (selectedTypes.contains(type))
                                            selectedTypes - type
                                        else selectedTypes + type
                                    }
                                )
                            }

                            HorizontalDivider()

                            Text("Accounts", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleSmall)
                            allAccounts.forEach { accountId ->
                                val accountName = accountNames[accountId] ?: accountId
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = selectedAccounts.contains(accountId),
                                                onCheckedChange = {
                                                    selectedAccounts = if (selectedAccounts.contains(accountId))
                                                        selectedAccounts - accountId
                                                    else selectedAccounts + accountId
                                                }
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(accountName)
                                        }
                                    },
                                    onClick = {
                                        selectedAccounts = if (selectedAccounts.contains(accountId))
                                            selectedAccounts - accountId
                                        else selectedAccounts + accountId
                                    }
                                )
                            }
                        }
                    }

                    val coroutineScope = rememberCoroutineScope()

                    IconButton(onClick = {
                        coroutineScope.launch { loadTransactions() }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        if(isLoading){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                groupedTransactions.forEach { (date) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 8.dp)
                        )
                    }
                    items(filteredTransactions) { transaction ->
                        TransactionCardItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.id) },
                            onDelete = { /* implement delete */ }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCardItem(
    transaction: Transaction?,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (isLoading) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator()
            }
        }
        return
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    onDelete()
                }) { Text("Yes") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDialog = true },
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (transaction!!.type) {
                    TransactionType.Expense -> Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    TransactionType.Transfer -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = transaction!!.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))

                val totalAmount = transaction.amount
                Text(
                    text = String.format("$%.2f", totalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = transaction!!.from,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Expense Flow",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = transaction.to,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}