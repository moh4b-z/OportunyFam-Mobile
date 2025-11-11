package com.oportunyfam_mobile_ong.Components.Cards

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CardAviso(
    pergunta: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Atenção!",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFB301)
            )
        },
        text = {
            Text(
                text = pergunta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Sim",
                    color = Color(0xFFFFB301)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Não",
                    color = Color.White
                )
            }
        },
        containerColor = Color(0xFF363534)
    )
}
