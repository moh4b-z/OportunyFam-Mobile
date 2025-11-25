package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.model.Crianca
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "ChildDetailDialog"

@Composable
fun ChildDetailDialog(
    child: Crianca?,
    onDismiss: () -> Unit,
    onStartConversation: (() -> Unit)? = null,
    onChildUpdated: ((Crianca) -> Unit)? = null
) {
    if (child == null) return

    var isEditMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados editáveis
    var editNome by remember { mutableStateOf(child.nome) }
    var editEmail by remember { mutableStateOf(child.email ?: "") }
    var editDataNascimento by remember { mutableStateOf(child.data_nascimento ?: "") }
    var editSexo by remember { mutableStateOf(child.sexo ?: "") }

    val scope = rememberCoroutineScope()

    // Função para salvar alterações
    fun salvarAlteracoes() {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val criancaService = RetrofitFactory().getCriancaService()

                // Buscar dados atuais
                val existingResp = withContext(Dispatchers.IO) {
                    criancaService.buscarPorId(child.crianca_id).execute()
                }

                if (existingResp.isSuccessful) {
                    val existing = existingResp.body()?.crianca

                    // Construir map com campos alterados
                    val fields = mutableMapOf<String, Any?>()
                    if (editNome.isNotBlank() && editNome != existing?.nome) fields["nome"] = editNome
                    if (editEmail.isNotBlank() && editEmail != existing?.email) fields["email"] = editEmail
                    if (editDataNascimento.isNotBlank() && editDataNascimento != existing?.data_nascimento) fields["data_nascimento"] = editDataNascimento
                    if (editSexo.isNotBlank() && editSexo != existing?.sexo) fields["sexo"] = editSexo

                    if (fields.isEmpty()) {
                        isLoading = false
                        errorMessage = "Nenhuma alteração detectada"
                        return@launch
                    }

                    // Atualizar
                    val updateResp = withContext(Dispatchers.IO) {
                        criancaService.atualizar(child.crianca_id, fields).execute()
                    }

                    if (updateResp.isSuccessful && updateResp.body()?.crianca != null) {
                        val updatedChild = updateResp.body()!!.crianca!!
                        isLoading = false
                        isEditMode = false
                        onChildUpdated?.invoke(updatedChild)
                        Log.d(TAG, "✅ Criança atualizada com sucesso")
                    } else {
                        isLoading = false
                        errorMessage = "Erro ao atualizar: ${updateResp.code()}"
                        Log.e(TAG, "Erro ao atualizar criança: ${updateResp.code()}")
                    }
                } else {
                    isLoading = false
                    errorMessage = "Erro ao buscar dados: ${existingResp.code()}"
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Erro: ${e.message}"
                Log.e(TAG, "Erro ao atualizar criança", e)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = !isEditMode,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header com botões fechar e editar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Editar Criança" else "Detalhes da Criança",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Botão Editar/Cancelar
                        IconButton(
                            onClick = {
                                if (isEditMode) {
                                    // Cancelar edição - restaurar valores
                                    editNome = child.nome
                                    editEmail = child.email ?: ""
                                    editDataNascimento = child.data_nascimento ?: ""
                                    editSexo = child.sexo ?: ""
                                    errorMessage = null
                                }
                                isEditMode = !isEditMode
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Cancelar" else "Editar",
                                tint = if (isEditMode) Color.Red else Color(0xFFFFA000)
                            )
                        }

                        IconButton(onClick = onDismiss, enabled = !isLoading) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Foto
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!child.foto_perfil.isNullOrEmpty()) {
                                AsyncImage(
                                    model = child.foto_perfil,
                                    contentDescription = "Foto de ${child.nome}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD27A))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.user),
                                            contentDescription = "Ícone padrão",
                                            modifier = Modifier.size(64.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Nome
                    item {
                        if (isEditMode) {
                            Column {
                                Text(
                                    text = "Nome *",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editNome,
                                    onValueChange = { editNome = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoading,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFA000),
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = "Nome",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = child.nome,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Email
                    item {
                        if (isEditMode) {
                            Column {
                                Text(
                                    text = "E-mail",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editEmail,
                                    onValueChange = { editEmail = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoading,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFA000),
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                            }
                        } else if (!child.email.isNullOrEmpty()) {
                            Column {
                                Text(
                                    text = "E-mail",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = child.email,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Data de Nascimento e Sexo
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Data de Nascimento
                            Column(modifier = Modifier.weight(1f)) {
                                if (isEditMode) {
                                    Text(
                                        text = "Data Nascimento",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editDataNascimento,
                                        onValueChange = { editDataNascimento = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading,
                                        singleLine = true,
                                        placeholder = { Text("AAAA-MM-DD", fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFFA000),
                                            unfocusedBorderColor = Color.LightGray
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Data Nascimento",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (!child.data_nascimento.isNullOrEmpty())
                                            formatarData(child.data_nascimento)
                                        else "Não informado",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }

                            // Sexo
                            Column(modifier = Modifier.weight(1f)) {
                                if (isEditMode) {
                                    Text(
                                        text = "Sexo",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = editSexo,
                                        onValueChange = { editSexo = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading,
                                        singleLine = true,
                                        placeholder = { Text("M/F", fontSize = 12.sp) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFFFA000),
                                            unfocusedBorderColor = Color.LightGray
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "Sexo",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = child.sexo ?: "Não informado",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Idade (apenas leitura - calculada)
                    if (!isEditMode) {
                        item {
                            Column {
                                Text(
                                    text = "Idade",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = child.idade?.let { "$it anos" } ?: "Não informado",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Cadastrado em (apenas leitura)
                    if (!isEditMode) {
                        item {
                            Column {
                                Text(
                                    text = "Cadastrado em",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatarData(child.criado_em),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Mensagem de erro
                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Botões de ação
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isEditMode) {
                        // Botão Salvar
                        Button(
                            onClick = { salvarAlteracoes() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Salvar Alterações", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Botão Iniciar Conversa
                        if (onStartConversation != null) {
                            Button(
                                onClick = { onStartConversation() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("Iniciar Conversa", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Botão Fechar
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA000))
                        ) {
                            Text("Fechar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatarData(data: String?): String {
    if (data.isNullOrBlank()) return "Não informado"
    return try {
        val partes = data.split("-", "T", " ")
        if (partes.size >= 3) {
            "${partes[2]}/${partes[1]}/${partes[0]}"
        } else {
            data
        }
    } catch (e: Exception) {
        data
    }
}

