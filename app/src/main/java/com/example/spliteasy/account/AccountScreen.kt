package com.example.spliteasy.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
//import com.example.spliteasy.transaction.TransactionCardItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    groupId: String = "",
    accountId: String = "",
    onBackClick: () -> Unit = {},
    onExpenditureClick: () -> Unit = {},
) {
    val db = FirebaseFirestore.getInstance()
    val accountState = remember { mutableStateOf<Account?>(null) }
    val userState = remember { mutableStateOf<User?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(accountId) {
        isLoading.value = true
        val account = Accounts.get(db, groupId,accountId)
        accountState.value = account
        if(account?.user == null) return@LaunchedEffect
        val user = Accounts.getUser(db,groupId,accountId)
        userState.value = user!!
        isLoading.value = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->

        if (isLoading.value || accountState.value == null || userState.value == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val account = accountState.value!!
            val user = userState.value!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item { AccountHeader(user) }

                item { HorizontalDivider() }

                item { BalanceSummary(account) }

                item {
                    ExpenditureSummary(
                        account = account,
                        onClick = onExpenditureClick
                    )
                }

                if (account.expenses.isNotEmpty()) {
                    item {
                        SectionTitle("Recent Expenses")
                    }
                    items(account.expenses.take(3)) { transaction ->
//                        TransactionCardItem(transaction, onClick = {})
                    }
                }

                if (account.transfers.isNotEmpty()) {
                    item {
                        SectionTitle("Recent Transfers")
                    }
                    items(account.transfers.take(3)) { transaction ->
//                        TransactionCardItem(transaction, onClick = {})
                    }
                }

                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun AccountHeader(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier.padding(4.dp)) {
            Text(
                text = user.email,
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