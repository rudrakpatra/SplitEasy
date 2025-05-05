package com.example.spliteasy.group

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spliteasy.api.Group
import com.example.spliteasy.api.Groups
import com.example.spliteasy.api.User
import com.example.spliteasy.api.Users
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun GroupsScreen(
    userId:String = "",
    onBackClick: () -> Unit = {},
    onAddGroupClick: () -> Unit = {},
    onGroupEnterClick: (group: Group) -> Unit = {},
    onGroupLeaveClick: (group: Group) -> Unit = {},
) {
    val db = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf<User>(User()) }
    var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    suspend fun loadGroups(){
        isLoading = true
        user = Users.get(db, userId)!!
        groups = Users.getGroups(db,user.id)
        isLoading = false
    }
    LaunchedEffect(userId) {
        loadGroups()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Groups") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions={
                    val coroutineScope = rememberCoroutineScope()
                    IconButton(onClick = {
                        coroutineScope.launch { loadGroups() }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload")
                    }
                }
            )

        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGroupClick,
                icon = { Icon(Icons.Outlined.Add, contentDescription = "Add Group") },
                text = { Text("Add Group") }
            )
        }
    ) { paddingValues ->
            if(isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                    ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }
            else {
                if(groups.isEmpty()){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No groups found")
                    }
                }
                else{
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(groups) { group ->
                            GroupItem(
                                group = group,
                                onEnter = onGroupEnterClick,
                                onLeave = onGroupLeaveClick
                            )
                        }
                    }
                }
            }
        }
    }



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupItem(
    group: Group,
    onEnter: (Group) -> Unit,
    onLeave: (Group) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Leave Group") },
            text = { Text("Are you sure you want to leave ${group!!.name}?") },
            confirmButton = {
                Button(onClick = {
                    onLeave(group!!)
                    showDialog = false
                }) {
                    Text("Leave")
                }
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
                onClick = { onEnter(group) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDialog = true
                }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleMedium)
            Text(text = group.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
