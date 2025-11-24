package com.oportunyfam_mobile.Screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oportunyfam_mobile.Components.AgendaHorizontal
import com.oportunyfam_mobile.Components.BarraTarefas
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.ViewModel.InscricaoViewModel
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.model.AulaDetalhada
import com.oportunyfam_mobile.model.InscricaoDetalhada
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.model.Usuario
import com.oportunyfam_mobile.MainActivity.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCriancaScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authDataStore = remember { AuthDataStore(context) }
    val inscricaoViewModel: InscricaoViewModel = viewModel()

    var isLoading by remember { mutableStateOf(true) }
    var aulasAgenda by remember { mutableStateOf<List<AulaDetalhada>>(emptyList()) }
    var criancas by remember { mutableStateOf<List<Crianca>>(emptyList()) }
    var inscricoesPorCrianca by remember { mutableStateOf<Map<Int, List<InscricaoDetalhada>>>(emptyMap()) }

    // Carrega dados iniciais
    LaunchedEffect(Unit) {
        isLoading = true
        val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }

        // Obter lista de crianças dependendo do tipo
        val childrenList = mutableListOf<Crianca>()
        if (auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO) {
            val usuarioId = (auth.user as? Usuario)?.usuario_id
            try {
                // network on IO
                val resp = withContext(Dispatchers.IO) {
                    RetrofitFactory().getUsuarioService().buscarPorId(usuarioId ?: 0).execute()
                }
                if (resp.isSuccessful) {
                    val usuario = resp.body()?.usuario
                    val mini = usuario?.criancas_dependentes ?: emptyList()
                    mini.forEach { c ->
                        childrenList.add(
                            Crianca(
                                crianca_id = c.id_crianca,
                                pessoa_id = c.id_pessoa,
                                nome = c.nome,
                                email = null,
                                foto_perfil = null,
                                data_nascimento = "",
                                idade = 0,
                                criado_em = "",
                                atualizado_em = null,
                                sexo = null,
                                atividades_matriculadas = emptyList(),
                                conversas = emptyList()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("StatusCriancaScreen", "Erro ao buscar usuário: ${e.message}", e)
            }
        } else if (auth?.type == com.oportunyfam_mobile.data.AuthType.CRIANCA) {
            (auth.user as? Crianca)?.let { child ->
                childrenList.add(child)
            }
        }

        criancas = childrenList

        // Para cada criança, buscar inscricoes; se endpoint específico não retornar tudo, tentar fallback buscando todas e filtrando
        val mapInscr = mutableMapOf<Int, List<InscricaoDetalhada>>()
        val activityIds = mutableSetOf<Int>()
        childrenList.forEach { c ->
            try {
                Log.d("StatusCriancaScreen", "🔎 Buscando inscrições para criança id=${c.crianca_id}")
                val resp = withContext(Dispatchers.IO) {
                    RetrofitFactory().getInscricaoService().buscarInscricoesPorCrianca(c.crianca_id).execute()
                }

                var list: List<InscricaoDetalhada> = emptyList()

                if (resp.isSuccessful) {
                    list = resp.body()?.inscricoes ?: emptyList()
                    Log.d("StatusCriancaScreen", "✅ Endpoint específico retornou ${list.size} inscrições para crianca ${c.crianca_id}")
                }

                // Se não retornou nada ou não foi successful, tentar fallback: buscar todas e filtrar por crianca_id
                if (list.isEmpty()) {
                    try {
                        Log.d("StatusCriancaScreen", "⚠️ Fallback: buscando todas as inscrições para filtrar por criança ${c.crianca_id}")
                        val allResp = withContext(Dispatchers.IO) { RetrofitFactory().getInscricaoService().buscarTodasInscricoes().execute() }
                        if (allResp.isSuccessful) {
                            val todas = allResp.body()?.inscricoes ?: emptyList()
                            list = todas.filter { it.crianca_id == c.crianca_id }
                            Log.d("StatusCriancaScreen", "✅ Fallback encontrou ${list.size} inscrições para crianca ${c.crianca_id}")
                        } else {
                            Log.w("StatusCriancaScreen", "❌ Fallback buscarTodasInscricoes falhou: ${allResp.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("StatusCriancaScreen", "Erro no fallback buscarTodasInscricoes: ${e.message}", e)
                    }
                }

                mapInscr[c.crianca_id] = list
                list.forEach { activityIds.add(it.atividade_id) }

            } catch (e: Exception) {
                Log.e("StatusCriancaScreen", "Erro ao buscar inscrições para crianca ${c.crianca_id}: ${e.message}", e)
                mapInscr[c.crianca_id] = emptyList()
            }
        }

        inscricoesPorCrianca = mapInscr

        // Buscar aulas relacionadas às atividades das inscrições (fallback: buscar todas aulas e filtrar)
        try {
            val aulasResp = withContext(Dispatchers.IO) {
                RetrofitFactory().getAtividadeService().buscarTodasAulas().execute()
            }
            if (aulasResp.isSuccessful) {
                val todas = aulasResp.body()?.aulas ?: emptyList()
                aulasAgenda = todas.filter { activityIds.contains(it.id_atividade) }
            } else {
                aulasAgenda = emptyList()
            }
        } catch (e: Exception) {
            Log.e("StatusCriancaScreen", "Erro ao buscar aulas: ${e.message}", e)
            aulasAgenda = emptyList()
        }

        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda") }) },
        bottomBar = { BarraTarefas(navController = navController, currentRoute = NavRoutes.STATUS_CRIANCA) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Box
            }

            // Se não houver crianças, mostrar aviso centralizado
            if (criancas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nenhuma criança cadastrada ainda.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { navController?.navigate(NavRoutes.CHILD_REGISTER) }) { Text("Cadastrar criança") }
                    }
                }
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Agenda horizontal no topo
                AgendaHorizontal(aulas = aulasAgenda, onDateSelected = { /* opcional: filtrar */ })

                Spacer(modifier = Modifier.height(8.dp))

                // Lista de crianças (cards expansíveis)
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(criancas) { crianca ->
                        var expanded by remember { mutableStateOf(false) }

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = crianca.nome, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    Text(text = if (expanded) "▲" else "▼")
                                }

                                if (expanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val inscricoes = inscricoesPorCrianca[crianca.crianca_id] ?: emptyList()
                                    if (inscricoes.isEmpty()) {
                                        Text("Nenhuma inscrição encontrada")
                                    } else {
                                        inscricoes.forEach { inscricao ->
                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = inscricao.atividade_titulo, style = MaterialTheme.typography.bodyLarge)
                                                    Text(text = "Status: ${inscricao.status_inscricao}", style = MaterialTheme.typography.bodySmall)
                                                }

                                                // Ações dependendo do tipo de usuário e status
                                                // status-specific UI handled below via buttons

                                                // Botões de ação: confirmar (para responsavel) ou deletar (para crianca caso sugerida)
                                                // Mostramos botões separadamente abaixo
                                                Column(horizontalAlignment = Alignment.End) {
                                                    // botão navegar para atividade
                                                    TextButton(onClick = { navController?.navigate("atividade/${inscricao.atividade_id}") }) { Text("Abrir") }

                                                    // Confirmar (responsável) - exibe se status_id == 1 e usuário é USUARIO
                                                    val isUsuario = remember { mutableStateOf(false) }
                                                    LaunchedEffect(Unit) {
                                                        val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                                        isUsuario.value = auth?.type == com.oportunyfam_mobile.data.AuthType.USUARIO
                                                    }

                                                    if (inscricao.status_id == 1 && isUsuario.value) {
                                                        Button(onClick = {
                                                            // confirmar inscrição: atualizar status de 1 -> 3
                                                            inscricaoViewModel.atualizarStatusInscricao(inscricao.inscricao_id, 3, inscricao.atividade_id)
                                                            // recarregar lista de inscricoes para essa crianca e recomputar aulasAgenda
                                                            scope.launch {
                                                                try {
                                                                    val resp = withContext(Dispatchers.IO) {
                                                                        RetrofitFactory().getInscricaoService().buscarInscricoesPorCrianca(crianca.crianca_id).execute()
                                                                    }
                                                                    if (resp.isSuccessful) {
                                                                        inscricoesPorCrianca = inscricoesPorCrianca.toMutableMap().also { it[crianca.crianca_id] = resp.body()?.inscricoes ?: emptyList() }
                                                                        // recompute activityIds across all children
                                                                        val activityIdsNow = inscricoesPorCrianca.values.flatten().map { it.atividade_id }.toSet()
                                                                        try {
                                                                            val aulasResp = withContext(Dispatchers.IO) {
                                                                                RetrofitFactory().getAtividadeService().buscarTodasAulas().execute()
                                                                            }
                                                                            if (aulasResp.isSuccessful) {
                                                                                val todas = aulasResp.body()?.aulas ?: emptyList()
                                                                                aulasAgenda = todas.filter { activityIdsNow.contains(it.id_atividade) }
                                                                            } else {
                                                                                aulasAgenda = emptyList()
                                                                            }
                                                                        } catch (e: Exception) {
                                                                            Log.e("StatusCriancaScreen", "Erro ao recomputar aulasAgenda: ${e.message}", e)
                                                                            aulasAgenda = emptyList()
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.e("StatusCriancaScreen", "Erro ao recarregar inscrições: ${e.message}", e)
                                                                }
                                                            }
                                                        }) { Text("Confirmar") }
                                                    }

                                                    // Deletar sugestão (se usuário for CRIANCA and status_id == 1)
                                                    val isCrianca = remember { mutableStateOf(false) }
                                                    LaunchedEffect(Unit) {
                                                        val auth = withContext(Dispatchers.IO) { authDataStore.loadAuthUser() }
                                                        isCrianca.value = auth?.type == com.oportunyfam_mobile.data.AuthType.CRIANCA
                                                    }

                                                    if (inscricao.status_id == 1 && isCrianca.value) {
                                                        Button(onClick = {
                                                            // deletar inscrição sugerida
                                                            scope.launch {
                                                                try {
                                                                    val call = withContext(Dispatchers.IO) {
                                                                        RetrofitFactory().getInscricaoService().deletarInscricao(inscricao.inscricao_id).execute()
                                                                    }
                                                                    if (call.isSuccessful) {
                                                                        // recarregar inscrições para a criança e recomputar aulasAgenda
                                                                        val resp = withContext(Dispatchers.IO) {
                                                                            RetrofitFactory().getInscricaoService().buscarInscricoesPorCrianca(crianca.crianca_id).execute()
                                                                        }
                                                                        if (resp.isSuccessful) {
                                                                            inscricoesPorCrianca = inscricoesPorCrianca.toMutableMap().also { it[crianca.crianca_id] = resp.body()?.inscricoes ?: emptyList() }
                                                                            val activityIdsNow = inscricoesPorCrianca.values.flatten().map { it.atividade_id }.toSet()
                                                                            try {
                                                                                val aulasResp = withContext(Dispatchers.IO) {
                                                                                    RetrofitFactory().getAtividadeService().buscarTodasAulas().execute()
                                                                                }
                                                                                if (aulasResp.isSuccessful) {
                                                                                    val todas = aulasResp.body()?.aulas ?: emptyList()
                                                                                    aulasAgenda = todas.filter { activityIdsNow.contains(it.id_atividade) }
                                                                                } else {
                                                                                    aulasAgenda = emptyList()
                                                                                }
                                                                            } catch (e: Exception) {
                                                                                Log.e("StatusCriancaScreen", "Erro ao recomputar aulasAgenda: ${e.message}", e)
                                                                                aulasAgenda = emptyList()
                                                                            }
                                                                        }
                                                                    }
                                                                } catch (e: Exception) {
                                                                    Log.e("StatusCriancaScreen", "Erro ao deletar inscrição: ${e.message}", e)
                                                                }
                                                            }
                                                        }) { Text("Remover") }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
