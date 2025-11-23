package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oportunyfam_mobile.model.Crianca

@Composable
fun PerfilTabs(selectedTab: String, filhos: List<Crianca>) {
    when (selectedTab) {
        "Informações" -> {
            // nothing here; parent shows InformacoesTab
        }
        "Filhos" -> {
            if (filhos.isEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().height(200.dp), verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Text("Nenhum filho cadastrado")
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filhos) { filho ->
                        ChildCard(child = filho)
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
