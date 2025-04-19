package com.example.spliteasy.group

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.spliteasy.api.Group


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AddGroupScreen(
    onAddButtonClicked: (group:Group) -> Unit={},
    onBackButtonClicked: () -> Unit={},
){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = " Add Group")
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
        AddGroupBody(
            modifier = Modifier.padding(innerPadding),
            onAddButtonClicked = onAddButtonClicked
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupBody(
    modifier: Modifier = Modifier,
    onAddButtonClicked: (group: Group) -> Unit
){
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it
                            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("name") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = groupDescription,
            onValueChange = { groupDescription = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("description") }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            onAddButtonClicked(Group(id="",name=groupName, description = groupDescription))
        }) {
            Text("Add Group")
        }
    }

}




