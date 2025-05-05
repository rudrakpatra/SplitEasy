package com.example.spliteasy.auth

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * A dialog component to confirm user logout
 *
 * @param onDismiss Called when the user dismisses the dialog
 * @param onConfirm Called when the user confirms they want to log out
 */
@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Log out?")
        },
        text = {
            Text(text = "Are you sure you want to log out and go back to login?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Log out")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}