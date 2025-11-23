// This file was intentionally removed. Edit profile UI is disabled.
// If you need to restore it later, look for previous commits or contact the author.
package com.oportunyfam_mobile.Components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.oportunyfam_mobile.model.Usuario
import kotlinx.coroutines.CoroutineScope

@Composable
fun EditarPerfilDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSave: (Usuario) -> Unit,
    usuarioService: Any,
    scope: CoroutineScope
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Perfil (edição desabilitada)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Nome: ${usuario.nome}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Email: ${usuario.email}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Telefone: ${usuario.telefone ?: "-"}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}
