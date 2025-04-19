package com.example.spliteasy.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Refresh

import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCardItem(
    transaction: Transaction,
    onClick: () -> Unit,
//    onDelete: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

//    if (showDialog) {
//        AlertDialog(
//            onDismissRequest = { showDialog = false },
//            title = { Text("Delete Transaction?") },
//            text = { Text("Are you sure you want to delete this transaction?") },
//            confirmButton = {
//                Button(onClick = {
//                    showDialog = false
//                    onDelete()
//                }) { Text("Yes") }
//            },
//            dismissButton = {
//                OutlinedButton(onClick = { showDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDialog = true },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (transaction.type) {
                    TransactionType.Expense -> Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Expense",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    TransactionType.Transfer -> Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Transfer",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = transaction.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                // Display amount summary
                val totalAmount = transaction.to.sumOf { it.second }
                Text(
                    text = String.format("$%.2f", totalAmount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(
                        alignment = Alignment.Bottom
                    )
                )
            }


            val fromNames = transaction.from.map { it.first.user.name }
            val toNames = transaction.to.map { it.first.user.name }
            val fromCount = fromNames.size
            val toCount = toNames.size
            val fromText = if (fromCount == 1) fromNames.first() else "$fromCount Members"
            val toText = if (toCount == 1) toNames.first() else "$toCount Members"

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = fromText,
                    modifier = Modifier
                        .padding(end = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Transfer",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    text = toText,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TransactionsScreen(
    group: Group= ExampleGroups[0],
    transactions: List<Transaction> = ExampleTransactions,
    onTransactionClick: (id: String) -> Unit={},
    onAddTransaction: () -> Unit={},
    onRefreshClick:()->Unit={},
    onBackClick:()->Unit={},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${transactions.size} Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions={
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh Transactions")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Add Transaction") },
                text = { Text("Add Transaction") }
            )
        },
    )
    { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {
            // Transactions list
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val groupedTransactions = transactions.sortedByDescending { it.timestamp }
                    .groupBy { it.timestamp.toDate() }
                // Format date to readable format
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ){
                    groupedTransactions.forEach { (date, transactionsInDay) ->
                        //sticky Version stickyHeader {}
                        //non sticky  item{}
                        item {
                            Text(
                                text = dateFormat.format(date),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(top = 8.dp)
                            )
                        }
                        items(transactionsInDay.size) { index ->
                            TransactionCardItem(
                                transaction = transactionsInDay[index],
                                onClick = { onTransactionClick(transactionsInDay[index].id) }
                            )
                        }
                    }
                }
            }
        }

    }

}