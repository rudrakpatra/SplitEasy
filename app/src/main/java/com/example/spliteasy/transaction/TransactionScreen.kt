package com.example.spliteasy.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.ExampleTransactions
import com.example.spliteasy.api.Member
import com.example.spliteasy.api.Transaction
import com.example.spliteasy.api.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun TransactionScreen(
    transaction: Transaction= ExampleTransactions[0],
    onBackClick: () -> Unit={},
    onEditClick: () -> Unit={},
    onDeleteClick: () -> Unit={}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Transaction")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Transaction")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Header
            item {
                TransactionHeader(transaction)
            }

            // Divider
            item {
                HorizontalDivider()
            }

            // From Section
            item {
                Text(
                    text = "FROM",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // From Members
            items(transaction.from) { (member, amount) ->
                MemberAmountItem(member = member, amount = amount)
            }

            // Divider between sections
            item {
                HorizontalDivider()
            }

            // To Section with arrow icon
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "TO",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // To Members
            items(transaction.to) { (member, amount) ->
                MemberAmountItem(member = member, amount = amount)
            }
        }
    }
}

@Composable
fun TransactionHeader(transaction: Transaction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Transaction Title
        Text(
            text = transaction.label,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Date
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        Row(modifier = Modifier.padding(4.dp)) {
            Text(
                text = dateFormat.format(transaction.timestamp.toDate()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }


        // Total Amount
        val totalAmount = transaction.to.sumOf { it.second }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = String.format("$%.2f", totalAmount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Transaction Type Indicator
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item{
                val type = when(transaction.type){
                    TransactionType.Expense -> "Expense"
                    TransactionType.Transfer -> "Transfer"
                }
                TagChip(tag = type)
            }
            items(transaction.tags) { tag ->
                Row(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TagChip(tag = tag)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: String){
    Row(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(tag)
            }
        }
    }

}

@Composable
fun MemberAmountItem(member: Member, amount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = member.user.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = String.format("$%.2f", amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}