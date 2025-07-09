package io.mityukov.geo.tracking.app.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.mityukov.geo.tracking.R

@Composable
fun CommonAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    showDismissButton: Boolean = true
) {
    AlertDialog(
        icon = {
            Icon(
                painterResource(R.drawable.icon_attention),
                contentDescription = null
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.dialog_yes))
            }
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.dialog_no))
                }
            }
        } else {
            null
        }
    )
}