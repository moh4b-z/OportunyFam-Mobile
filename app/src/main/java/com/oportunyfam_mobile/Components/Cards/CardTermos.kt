package com.oportunyfam_mobile.Components.Cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CardTermos(
    title: String,
    summary: String,
    terms: String,
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Area rolável com os termos (altura fixa para parecer um resumo dentro do card)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.Transparent)
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())) {
                    Text(
                        text = terms,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAcceptedChange(!accepted) }
                    .padding(end = 8.dp)
            ) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = onAcceptedChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Eu li e concordo com os termos", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "(Ex: política de privacidade e termos de uso)", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ações
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = onBack) {
                    Text(text = "Voltar")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardTermosPreview() {
    var accepted by remember { mutableStateOf(false) }

    val sampleTerms = buildString {
        append("Ao utilizar este aplicativo, você concorda com os Termos de Serviço e com a Política de Privacidade. ")
        append("Este texto é uma amostra e inclui pontos comuns encontrados em apps: coleta de dados, uso de analytics, política de cancelamento e limites de responsabilidade. ")
        append("Os dados coletados podem incluir informações pessoais necessárias para fornecer e melhorar o serviço. ")
        append("Você pode revogar o consentimento a qualquer momento, contatando nosso suporte. ")
        append("Leia atentamente para entender como seus dados são usados. ")
        repeat(6) { append("\n\nContinuação dos termos de serviço - conteúdo de exemplo para demonstrar scroll e legibilidade.") }
    }

    CardTermos(
        title = "Termos de Serviço",
        summary = "Leia os principais pontos antes de continuar. Este resumo mostra um preview dos termos.",
        terms = sampleTerms,
        accepted = accepted,
        onAcceptedChange = { accepted = it },
        onBack = { /* ação de voltar */ },
        modifier = Modifier
    )
}

