package com.example.spliteasy.member

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spliteasy.api.Account
import com.example.spliteasy.api.ExampleGroups
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.Member
import com.example.spliteasy.api.Transaction
import com.example.spliteasy.api.User

data class User(val name: String, val email: String, val id: String)
data class Account(
    val balance: Double = 0.0,
    val expenditure: Double = 0.0,
    val creditList: List<Transaction> = emptyList(),
    val debitList: List<Transaction> = emptyList()
)
data class Member(val id: String, val user: User, val account: Account)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AddMemberScreen(
    group: Group = ExampleGroups[0],
    onAddButtonClicked: (member: Member) -> Unit = {},
    onBackButtonClicked: () -> Unit = {},
    availableFriends: List<User> = sampleFriends
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Add Member")
                },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        AddMemberBody(
            modifier = Modifier.padding(innerPadding),
            onAddButtonClicked = onAddButtonClicked,
            availableFriends = availableFriends
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberBody(
    modifier: Modifier = Modifier,
    onAddButtonClicked: (member: Member) -> Unit,
    availableFriends: List<User>
) {
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select a friend to add",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    value = selectedUser?.name ?: "",
                    onValueChange = {},
                    label = { Text("Select Friend") },
                    trailingIcon = {
                        Icon(
                            imageVector = if (isDropdownExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isDropdownExpanded) "Collapse" else "Expand"
                        )
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    availableFriends.forEach { friend ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(friend.name)
                                    Text(
                                        text = friend.email,
                                        style = TextStyle(fontSize = 12.sp)
                                    )
                                }
                            },
                            onClick = {
                                selectedUser = friend
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                selectedUser?.let { user ->
                    val newMember = Member(
                        id = "",
                        user = user,
                        account = Account()
                    )
                    onAddButtonClicked(newMember)
                }
            },
            enabled = selectedUser != null
        ) {
            Text("Add Member")
        }
    }
}

// Sample data for preview
val sampleFriends = listOf(
    User(name = "Alice Smith", email = "alice@example.com", id = "user1"),
    User(name = "Bob Johnson", email = "bob@example.com", id = "user2"),
    User(name = "Carol Williams", email = "carol@example.com", id = "user3"),
    User(name = "David Brown", email = "david@example.com", id = "user4")
)