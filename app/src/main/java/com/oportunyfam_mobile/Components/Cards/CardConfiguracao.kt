package com.oportunyfam_mobile.Components.Cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile.model.Configuracao

/**
 * Card de configuração
 */
@Composable
fun CardConfiguracao(config: Configuracao) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(config.titulo, fontWeight = FontWeight.Bold)
                Text(config.descricao, fontSize = 14.sp, color = Color.Gray)
            }
            when (config) {
                is Configuracao.Toggle -> {
                    Switch(
                        checked = config.valor,
                        onCheckedChange = { /* TODO */ },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFA000)
                        )
                    )
                }
                is Configuracao.Info -> {
                    Text(config.valor, color = Color.Gray)
                }
            }
        }
    }
}