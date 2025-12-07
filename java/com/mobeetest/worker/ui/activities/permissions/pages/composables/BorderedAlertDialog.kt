package com.mobeetest.worker.ui.activities.permissions.pages.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mobeetest.worker.ui.theme.MobeetestTheme

@Composable
fun BorderedAlertDialog(
    title: String,
    message: String,
    positiveButtonText: String = "OK",
    cancelable: Boolean = false,
    onPositiveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (cancelable) onDismiss()
        }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onPositiveClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(positiveButtonText)
                }


            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBorderedAlertDialog() {
    MobeetestTheme {
        var showDialog by remember { mutableStateOf(true) }

        if (showDialog) {
            BorderedAlertDialog(
                title = "Ignore Battery Optimizations",
                message = "Please press \"Allow\" or select \"No restrictions\" from the menu.",
                positiveButtonText = "Next",
                cancelable = false,
                onPositiveClick = {
                    //showDialog = false
                },
                onDismiss = {
                    //showDialog = false
                }
            )
        }
    }
}
