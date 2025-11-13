package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile.Service.CriancaService
import com.oportunyfam_mobile.model.Crianca
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChildrenListSection(
    usuarioId: Int,
    criancaService: CriancaService,
    onChildClick: (Crianca) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var crianças by remember { mutableStateOf<List<Crianca>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Carregar crianças quando o componente é inicializado
    LaunchedEffect(usuarioId) {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    criancaService.listarPorUsuario(usuarioId).execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    val criancasList = response.body()!!.criancas.map { raw ->
                        Crianca(
                            crianca_id = raw.id,
                            pessoa_id = raw.id,
                            nome = raw.nome,
                            email = raw.email,
                            foto_perfil = raw.foto_perfil,
                            data_nascimento = raw.data_nascimento ?: "",
                            idade = calcularIdade(raw.data_nascimento ?: ""),
                            criado_em = raw.criado_em ?: "",
                            atualizado_em = null,
                            sexo = when (raw.id_sexo) {
                                1 -> "Masculino"
                                2 -> "Feminino"
                                3 -> "Outro"
                                else -> "Não informado"
                            },
                            id = raw.id
                        )
                    }
                    crianças = criancasList
                } else {
                    errorMessage = "Erro ao carregar crianças"
                    Log.e("ChildrenList", "Erro: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Erro de conexão"
                Log.e("ChildrenList", "Exceção: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Título
        Text(
            text = "Meus Filhos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        when {
            isLoading -> {
                // Loading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFAF25))
                }
            }

            !errorMessage.isNullOrEmpty() -> {
                // Erro
                Text(
                    text = errorMessage ?: "Erro desconhecido",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            crianças.isEmpty() -> {
                // Lista vazia
                Text(
                    text = "Nenhuma criança cadastrada ainda",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            else -> {
                // Lista de crianças
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(crianças) { child ->
                        ChildCard(
                            child = child,
                            onClick = { onChildClick(child) }
                        )
                    }
                }
            }
        }
    }
}

private fun calcularIdade(dataNascimento: String): Int {
    return try {
        val partes = dataNascimento.split("-", "/")
        if (partes.size >= 3) {
            val ano = partes[0].toIntOrNull() ?: return 0
            val anoAtual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            anoAtual - ano
        } else {
            0
        }
    } catch (e: Exception) {
        0
    }
}
