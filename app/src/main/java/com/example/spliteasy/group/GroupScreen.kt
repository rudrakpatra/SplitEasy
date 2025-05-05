package com.example.spliteasy.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.Groups
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GroupScreen(
    groupId: String="",
    groupDefault: Group= Group(),
    groupDescription: String="",
    onBackClick: () -> Unit={},
    onAccountsClick: () -> Unit={},
    onTransactionsClick: () -> Unit={},
    onSummaryClick: () -> Unit={},
    onAIQueryClick:()->Unit={},
) {
    val db = FirebaseFirestore.getInstance()

    var group = remember {
        mutableStateOf(groupDefault)
    }
    var isLoading = remember {
        mutableStateOf(true)
    }
    val context = LocalContext.current
    LaunchedEffect(groupId) {
        group.value = Groups.get(db,groupId) ?: Group()
        isLoading.value = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group.value.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip =
                                    ClipData.newPlainText("Invite Link", "SplitEasy:${groupId}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Invite Link Copied", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share Group")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                ExtendedFloatingActionButton(
                    onClick = onAIQueryClick,
                    icon = { Icon(Icons.Rounded.Create, "AI Query") },
                    text = { Text("AI Query") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = group.value.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CardItem("Accounts", onAccountsClick)
            CardItem("Transactions", onTransactionsClick)
            CardItem("Summary", onSummaryClick)
        }
    }
}

@Composable
fun CardItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
