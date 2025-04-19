package com.example.spliteasy.member

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.ExampleMembers
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.Member
import kotlin.math.abs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MemberCardItem(member: Member, onClick: () -> Unit, onRemove: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Remove this Member?") },
            text = { Text("Are you sure you want remove this member?") },
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
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = member.user.name, style = MaterialTheme.typography.titleMedium)
            val balance = member.account.balance
            val color = when{
                balance>0 -> MaterialTheme.colorScheme.primary
                balance<0 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
            }
            val sign = when{
                balance>0 -> "+"
                balance<0 -> "-"
                else -> ""
            }
            val amt = abs(member.account.balance);
            Text(text = String.format(sign+"\$%.2f",amt), style = MaterialTheme.typography.titleMedium , color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun MembersScreen(
    group: Group= Group("Family Trip", "Group for our summer vacation", "family-trip-1"),
    members: List<Member> = ExampleMembers,
    onBackClick: () -> Unit={},
    onAddMemberClick: () -> Unit={},
    onMemberClick: (id: String) -> Unit={},
    onMemberRemove: (id: String) -> Unit={}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${members.size} Members") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMemberClick,
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Add Member") },
                text = { Text("Add Member") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(members.size) { index ->
                val member = members[index]
                MemberCardItem(
                    member = member,
                    onClick = { onMemberClick(member.id) },
                    onRemove = { onMemberRemove(member.id) }
                )
            }
        }
    }
}

