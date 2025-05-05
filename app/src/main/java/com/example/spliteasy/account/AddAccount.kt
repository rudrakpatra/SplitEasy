package com.example.spliteasy.account

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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.spliteasy.api.User
import com.example.spliteasy.api.Users
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AddAccountScreen(
    userId: String="",
    groupId: String="",
    onUserSelected: (user:User) -> Unit = {},
    onBackButtonClicked: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Add Account")
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
        Column(
            modifier = Modifier.padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddFriend(
                userId = userId,
                onFriendSelected = onUserSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriend(
    userId: String="",
    onFriendSelected: (user: User) -> Unit,
) {
    val db = FirebaseFirestore.getInstance()
    val friends = remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect (userId){
        friends.value = Users.getFriends(db, userId =userId )
    }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
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
                friends.value.forEach { friend ->
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
                onFriendSelected(user)
            }
        },
        enabled = selectedUser != null
    ) {
        Text("Add Account")
    }
}