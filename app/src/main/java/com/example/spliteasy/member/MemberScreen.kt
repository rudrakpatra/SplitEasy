package com.example.spliteasy.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Account
import com.example.spliteasy.api.ExampleGroups
import com.example.spliteasy.api.ExampleMembers
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.Member
import com.example.spliteasy.transaction.TransactionCardItem
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MemberScreen(
    group: Group= ExampleGroups[0],
    member: Member= ExampleMembers[0],
    onBackClick: () -> Unit={},
    onExpenditureClick: () -> Unit={},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Member Header
            item {
                MemberHeader(member)
            }

            // Divider
            item {
                HorizontalDivider()
            }

            // Balance Summary
            item {
                BalanceSummary(member.account)
            }
            // Expenditure Summary
            item {
                ExpenditureSummary(
                    member.account,
                    onClick = onExpenditureClick
                )
            }

            // Recent Credits
            if (member.account.creditList.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Credits",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(member.account.creditList.take(3)) { transaction ->
                    TransactionCardItem(transaction, onClick = {})
                }
            }

            // Recent Debits
            if (member.account.debitList.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Debits",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(member.account.debitList.take(3)) { transaction ->
                    TransactionCardItem(transaction, onClick = {})
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun MemberHeader(member: Member) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Member Name
        Text(
            text = member.user.name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        // Email
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        Row(modifier = Modifier.padding(4.dp)) {
            Text(
                text = member.user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun BalanceSummary(account:Account) {
    val balance = account.balance
    val color = when{
        balance>0 -> MaterialTheme.colorScheme.primary
        0>balance -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }
    val sign = when{
        balance>0 -> "+"
        balance<0 -> "-"
        else -> ""
    }
    val amt = abs(balance)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "Net Balance",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = String.format(sign+"\$%.2f", amt),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )


            Text(
                text =  when{
                    balance>0 -> "You are owed money"
                    balance<0 -> "You owe money"
                    else -> "You are completely even"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExpenditureSummary(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Total Expenditure of",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format("$%.2f", account.expenditure),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See all related transactions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Show all transactions"
                )
            }
        }
    }
}