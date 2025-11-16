package com.oportunyfam_mobile.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile.model.AulaDetalhada // Importe sua classe
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ========================================================================
// Cores da Aplicação
// ========================================================================

val LaranjaApp = Color(0xFFF69508)
val AmareloApp = Color(0xFFFFC14D)
val CremeSuaveApp = Color(0xFFFFF1D2)
val TextoPadrao = Color(0xFF333333)
val TextoSuave = Color.Gray

// ========================================================================
// Componente Principal - Agenda Horizontal
// ========================================================================

/**
 * Exibe um calendário horizontal (LazyRow) dos próximos 30 dias.
 * Mostra uma notificação nos dias que têm aulas.
 *
 * @param aulas Lista de todas as aulas (usada para gerar as notificações).
 * @param onDateSelected Callback disparado quando uma data é selecionada. Retorna a data (LocalDate).
 */
@Composable
fun AgendaHorizontal(
    aulas: List<AulaDetalhada>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // Define o range de datas (Hoje + 30 dias)
    val diasParaMostrar = 30
    val datas by remember {
        mutableStateOf(
            (0L until diasParaMostrar).map { LocalDate.now().plusDays(it) }
        )
    }

    // Processa a lista de aulas para um mapa [Data -> Contagem]
    // Isso é muito mais eficiente do que verificar a lista inteira para cada dia.
    val aulasPorDia: Map<LocalDate, Int> by remember(aulas) {
        derivedStateOf {
            aulas
                .groupBy {
                    // Converte a string "YYYY-MM-DD" para LocalDate
                    try {
                        LocalDate.parse(it.data_aula, DateTimeFormatter.ISO_LOCAL_DATE)
                    } catch (e: Exception) {
                        null // Ignora datas inválidas
                    }
                }
                .filterKeys { it != null }
                .mapValues { it.value.size }
                .mapKeys { it.key!! }
        }
    }

    // Guarda a data selecionada. Começa com a data de hoje.
    var dataSelecionada by remember { mutableStateOf(LocalDate.now()) }

    // Garante que o callback seja chamado na primeira renderização
    LaunchedEffect(Unit) {
        onDateSelected(dataSelecionada)
    }

    val lazyListState = rememberLazyListState()

    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(datas, key = { it.toString() }) { data ->
            val temAulas = (aulasPorDia[data] ?: 0) > 0
            val isSelected = data == dataSelecionada

            DiaCalendarioItem(
                data = data,
                temAulas = temAulas,
                isSelected = isSelected,
                onDateClick = {
                    dataSelecionada = it
                    onDateSelected(it) // Dispara a ação para a "futura página"
                }
            )
        }
    }
}

// ========================================================================
// Componente de Item - Dia do Calendário
// ========================================================================

/**
 * Um item de dia individual para a agenda horizontal.
 *
 * @param data O LocalDate que este item representa.
 * @param temAulas Boolean se deve mostrar a "bolinha" de notificação.
 * @param isSelected Boolean se este item está selecionado.
 * @param onDateClick Callback quando este item é clicado.
 */
@Composable
fun DiaCalendarioItem(
    data: LocalDate,
    temAulas: Boolean,
    isSelected: Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    // Formatadores para "QUI" e "13"
    val formatadorDiaSemana = remember { DateTimeFormatter.ofPattern("E") }
    val formatadorDiaMes = remember { DateTimeFormatter.ofPattern("d") }

    val corFundo = if (isSelected) CremeSuaveApp else Color.White
    val corBorda = if (isSelected) LaranjaApp else Color.LightGray.copy(alpha = 0.7f)
    val corTextoDia = if (isSelected) LaranjaApp else TextoPadrao
    val corTextoSemana = if (isSelected) LaranjaApp else TextoSuave

    Box(
        modifier = Modifier
            .width(60.dp) // Largura fixa para cada item
            .clip(RoundedCornerShape(16.dp))
            .background(corFundo)
            .border(1.dp, corBorda, RoundedCornerShape(16.dp))
            .clickable { onDateClick(data) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // "Bolinha" de notificação no topo (baseado na imagem)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (temAulas) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(LaranjaApp, CircleShape)
                    )
                }
            }

            // Dia do Mês (ex: "13")
            Text(
                text = data.format(formatadorDiaMes),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = corTextoDia,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dia da Semana (ex: "QUI")
            Text(
                // Pega o nome do dia da semana e capitaliza (ex: "qui")
                text = data
                    .dayOfWeek
                    .getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                    .uppercase(),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = corTextoSemana,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ========================================================================
// Preview para o Android Studio
// ========================================================================

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AgendaHorizontalPreview() {
    // Dados de exemplo para o preview
    val aulasExemplo = listOf(
        // Simula uma aula hoje
        AulaDetalhada(
            aula_id = 1,
            id_atividade = 1,
            data_aula = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            hora_inicio = "09:00:00",
            hora_fim = "10:00:00",
            vagas_total = 10,
            vagas_disponiveis = 5,
            status_aula = "Futura",
            iram_participar = null,
            foram = null,
            ausentes = null
        ),
        // Simula duas aulas daqui a 2 dias
        AulaDetalhada(
            aula_id = 2,
            id_atividade = 1,
            data_aula = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE),
            hora_inicio = "10:00:00",
            hora_fim = "11:00:00",
            vagas_total = 10,
            vagas_disponiveis = 5,
            status_aula = "Futura",
            iram_participar = null,
            foram = null,
            ausentes = null
        ),
        AulaDetalhada(
            aula_id = 3,
            id_atividade = 2,
            data_aula = LocalDate.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE),
            hora_inicio = "14:00:00",
            hora_fim = "15:00:00",
            vagas_total = 10,
            vagas_disponiveis = 5,
            status_aula = "Futura",
            iram_participar = null,
            foram = null,
            ausentes = null
        ),
        // Simula uma aula daqui a 5 dias
        AulaDetalhada(
            aula_id = 4,
            id_atividade = 3,
            data_aula = LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE),
            hora_inicio = "09:00:00",
            hora_fim = "10:00:00",
            vagas_total = 10,
            vagas_disponiveis = 5,
            status_aula = "Futura",
            iram_participar = null,
            foram = null,
            ausentes = null
        )
    )

    MaterialTheme {
        Column {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AgendaHorizontal(
                aulas = aulasExemplo,
                onDateSelected = {
                    // No preview, não faz nada
                    println("Data selecionada: $it")
                }
            )
        }
    }
}