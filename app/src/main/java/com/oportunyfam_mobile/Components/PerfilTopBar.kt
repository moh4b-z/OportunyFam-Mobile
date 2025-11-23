package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun PerfilTopBar(navController: NavHostController?, onEdit: () -> Unit, onLogout: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = { navController?.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
        }
        IconButton(onClick = onLogout) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
        }
    }
}

