package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oportunyfam_mobile.model.Crianca

@Composable
fun PerfilTabs(selectedTab: String, criancas: List<Crianca>) {
    when (selectedTab) {
        "Informações" -> {
            // nothing here; parent shows InformacoesTab
        }
        "Crianças" -> {
            if (criancas.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Text("Nenhuma criança cadastrada")
                }
            } else {
                // Render as vertical list so it composes well inside the parent LazyColumn
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    criancas.forEach { crianca ->
                        ChildCard(child = crianca)
                    }
                }
            }
        }
        "Responsáveis" -> {
            Column(modifier = Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Person, contentDescription = null)
                Text("Responsáveis não disponíveis")
            }
        }
    }
}
