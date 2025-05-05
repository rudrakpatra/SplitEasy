package com.example.spliteasy.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Account
import com.example.spliteasy.api.Accounts
import com.example.spliteasy.api.Groups
import com.example.spliteasy.api.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    groupId: String,
    onBackClick: () -> Unit = {},
    onAddAccountClick: () -> Unit = {},
    onAccountClick: (id: String) -> Unit = {},
    onAccountRemove: (id: String) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(groupId) {
        isLoading = true
        accounts = Groups.getAccounts(db, groupId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${accounts.size} Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                })
                 },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = onAddAccountClick,
                        icon = { Icon(Icons.Outlined.Add, contentDescription = "Add Account") },
                        text = { Text("Add Account") }
                    )
                }
            ) { paddingValues ->
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(accounts.size) { index ->
                            val account = accounts[index]
                            AccountCardItem(
                                account = account,
                                onClick = { onAccountClick(account.id) },
                                onRemove = { onAccountRemove(account.id) }
                            )
                        }
                    }
                }
            }
        }

        @OptIn(ExperimentalFoundationApi::class)
        @Composable
        fun AccountCardItem(account: Account, onClick: () -> Unit, onRemove: () -> Unit) {
            var showDialog by remember { mutableStateOf(false) }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Remove this Account?") },
                    text = { Text("Are you sure you want to remove this account?") },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            onRemove()
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    val balance = account.balance
                    val color = when {
                        balance > 0 -> MaterialTheme.colorScheme.primary
                        balance < 0 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    val sign = when {
                        balance > 0 -> "+"
                        balance < 0 -> "-"
                        else -> ""
                    }
                    val amt = abs(balance)

                    Text(
                        text = String.format("$sign\$%.2f", amt),
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }
            }
        }
