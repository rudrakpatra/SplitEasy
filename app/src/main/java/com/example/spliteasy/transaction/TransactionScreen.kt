package com.example.spliteasy.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Expense
import com.example.spliteasy.api.Expenses
import com.example.spliteasy.api.Transaction
import com.example.spliteasy.api.TransactionType
import com.example.spliteasy.api.Transfer
import com.example.spliteasy.api.Transfers
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    groupId: String,
    transaction:Transaction,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val db=FirebaseFirestore.getInstance()
    var expense by remember { mutableStateOf<Expense?>(null) }
    var transfer by remember { mutableStateOf<Transfer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    suspend fun load() {
        isLoading=true
        when (transaction.type) {
            TransactionType.Expense -> {
                expense = Expenses.get(db, groupId, transaction.id)
            }
            TransactionType.Transfer -> {
                transfer = Transfers.get(db, groupId, transaction.id)
            }
            else -> {
                return
            }
        }
    }
    LaunchedEffect(transaction.id) {
        load()
    }
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
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        if(isLoading){
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }else if(expense!==null){
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .padding(16.dp),
            ){
                TransactionHeader(transaction = transaction)
                Spacer(modifier = Modifier.height(16.dp))
                DetailItem(label = "Amount", value = transaction.amount.toString())
                Spacer(modifier = Modifier.height(8.dp))
                DetailItem(label = "Type", value = transaction.type.toString())
                Spacer(modifier = Modifier.height(8.dp))
                //Expense Specific
                Column {
                    Text(
                        text = "paid by",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
//                        items(expense!!.paidBy) { payer ->
//                            DetailItem(label = payer.account, value = transaction.type.toString())
//                        }
                    }

                }
            }
        }else if (transfer!==null){
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .padding(16.dp),
            ){

            }
        }
        else{
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text("The transaction has been deleted or does not exist")
            }
        }
    }
}

@Composable
fun TransactionHeader(transaction: Transaction) {
    val dateFormat = remember {
        SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = transaction.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dateFormat.format(transaction.createdOn.toDate()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
