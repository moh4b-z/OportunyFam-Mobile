package com.oportunyfam_mobile.Components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.oportunyfam_mobile.Service.UsuarioService
import com.oportunyfam_mobile.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import android.util.Log

@Composable
fun EditarPerfilDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSave: (Usuario) -> Unit,
    usuarioService: UsuarioService,
    scope: CoroutineScope
) {
    val context = LocalContext.current

    // Debug: Log para verificar se os IDs estão sendo desserializados corretamente (se usuario.id for 0 = problema na desserialização)
    Log.d("EditarPerfilDialog", "Usuario ID: ${usuario.id}, Usuario_ID: ${usuario.usuario_id}")

    var fotoPerfil by remember { mutableStateOf(usuario.foto_perfil ?: "") }
    var telefone by remember { mutableStateOf(usuario.telefone ?: "") }
    // Campos editáveis adicionais (Usuario fields are non-nullable)
    var nome by remember { mutableStateOf(usuario.nome) }
    var email by remember { mutableStateOf(usuario.email) }
    var dataNascimento by remember { mutableStateOf(usuario.data_nascimento) }
    var cpf by remember { mutableStateOf(usuario.cpf) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para galeria de fotos -> faz upload imediato para Azure e registra no Logcat
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            // Faz upload em background e atualiza fotoPerfil com a URL retornada
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    // Cria arquivo temporário
                    val file = createImageFile(context)
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    if (inputStream == null) {
                        errorMessage = "Não foi possível ler a imagem selecionada"
                        return@launch
                    }
                    withContext(Dispatchers.IO) {
                        java.io.FileOutputStream(file).use { out ->
                            inputStream.use { inp -> inp.copyTo(out) }
                        }
                    }

                    // Verifica SAS token
                    val sas = com.oportunyfam_mobile.Config.AzureConfig.getStorageKey()
                    Log.d("EditarPerfilDialog", "Azure SAS token present: ${!sas.isNullOrBlank()}")
                    if (sas.isNullOrBlank()) {
                        errorMessage = "Azure Storage não configurado"
                        return@launch
                    }

                    Log.d("EditarPerfilDialog", "Iniciando upload para Azure (arquivo=${file.name})")
                    val uploadedUrl = com.oportunyfam_mobile.Service.AzureBlobRetrofit.uploadImageToAzure(
                        file,
                        com.oportunyfam_mobile.Config.AzureConfig.STORAGE_ACCOUNT,
                        sas,
                        com.oportunyfam_mobile.Config.AzureConfig.CONTAINER_PERFIL
                    )

                    if (!uploadedUrl.isNullOrBlank()) {
                        Log.d("EditarPerfilDialog", "Upload Azure bem-sucedido: $uploadedUrl")
                        fotoPerfil = uploadedUrl
                        // Envia apenas o campo foto_perfil ao backend (PUT parcial com mapa)
                        try {
                            val usuarioServiceLocal = usuarioService
                            val patchMap = mapOf("foto_perfil" to uploadedUrl)
                            val resp = withContext(Dispatchers.IO) { usuarioServiceLocal.atualizar(usuario.id, patchMap).execute() }
                            if (resp.isSuccessful) {
                                Log.d("EditarPerfilDialog", "PUT foto_perfil bem-sucedido")
                            } else {
                                Log.e("EditarPerfilDialog", "PUT foto_perfil falhou: ${resp.code()}")
                                errorMessage = "Erro ao atualizar foto no servidor"
                            }
                        } catch (e: Exception) {
                            Log.e("EditarPerfilDialog", "Erro ao enviar foto ao servidor", e)
                            errorMessage = "Erro ao enviar foto ao servidor: ${e.message}"
                        }
                    } else {
                        Log.e("EditarPerfilDialog", "Falha no upload para Azure")
                        errorMessage = "Falha no upload da imagem"
                    }
                } catch (e: Exception) {
                    Log.e("EditarPerfilDialog", "Erro ao enviar imagem para Azure", e)
                    errorMessage = "Erro ao enviar imagem: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Launcher para câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) {
        // A URI já foi salva quando criamos o arquivo no cache
        // Se necessário, pode adicionar lógica adicional de processamento aqui
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color.White)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header com close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Perfil",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== SEÇÃO: FOTO DE PERFIL =====
                Text(
                    text = "Foto de Perfil",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Card da foto com overlay para alterar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoPerfil.isNotEmpty()) {
                        AsyncImage(
                            model = fotoPerfil,
                            contentDescription = "Foto do Perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Adicionar foto",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Overlay com botões
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Galeria", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val file = createImageFile(context)
                                val uri = Uri.fromFile(file)
                                cameraLauncher.launch(uri)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Câmera", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== SEÇÃO: NOME =====
                Text(
                    text = "Nome",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = "Nome",
                    readOnly = isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ===== SEÇÃO: EMAIL =====
                Text(
                    text = "Email",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    readOnly = isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ===== SEÇÃO: DATA NASCIMENTO =====
                Text(
                    text = "Data de Nascimento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = dataNascimento,
                    onValueChange = { dataNascimento = it },
                    label = "Data de Nascimento",
                    readOnly = isLoading,
                    supportingText = { Text("YYYY-MM-DD") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ===== SEÇÃO: CPF =====
                Text(
                    text = "CPF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = cpf,
                    onValueChange = { cpf = it.filter { ch -> ch.isDigit() } },
                    label = "CPF",
                    readOnly = isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ===== SEÇÃO: TELEFONE =====
                Text(
                    text = "Número de Telefone",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it.filter { char -> char.isDigit() || char in "()- " } },
                    label = "Telefone",
                    readOnly = isLoading,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    ),
                    supportingText = { Text("(XX) XXXXX-XXXX") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Endereço removido conforme solicitado
                Spacer(modifier = Modifier.height(12.dp))

                // Mensagem de erro
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Botões de ação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        enabled = !isLoading
                    ) {
                        Text("Cancelar", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            if (telefone.isEmpty()) {
                                errorMessage = "Telefone é obrigatório"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null

                            // Chamada à API para atualizar usando coroutines (C)
                            scope.launch {
                                try {
                                    val usuarioServiceLocal = usuarioService
                                    // Busca versão atual do usuário para evitar sobrescrita (A)
                                    val existingResp = withContext(Dispatchers.IO) { usuarioServiceLocal.buscarPorId(usuario.id).execute() }
                                    if (existingResp.isSuccessful) {
                                        val existing = existingResp.body()?.usuario
                                        // Build a map of fields to update (partial PUT)
                                        val fields = mutableMapOf<String, Any?>()
                                        if (nome != existing?.nome) fields["nome"] = nome
                                        if (fotoPerfil.isNotBlank() && fotoPerfil != existing?.foto_perfil) fields["foto_perfil"] = fotoPerfil
                                        if (email != existing?.email) fields["email"] = email
                                        if (dataNascimento != existing?.data_nascimento) fields["data_nascimento"] = dataNascimento
                                        if (telefone.filter { it.isDigit() } != (existing?.telefone ?: "")) fields["telefone"] = telefone.filter { it.isDigit() }
                                        if (cpf != existing?.cpf) fields["cpf"] = cpf
                                        // endereco removed - do not include logradouro in partial update

                                        val updateResp = withContext(Dispatchers.IO) { usuarioServiceLocal.atualizar(usuario.id, fields).execute() }
                                        if (updateResp.isSuccessful && updateResp.body()?.usuario != null) {
                                            val usuarioAtualizado = updateResp.body()!!.usuario!!
                                            isLoading = false
                                            onSave(usuarioAtualizado)
                                            onDismiss()
                                        } else {
                                            errorMessage = "Erro ao atualizar perfil. Tente novamente."
                                            isLoading = false
                                        }
                                    } else {
                                        errorMessage = "Erro ao buscar dados do usuário: ${existingResp.code()}"
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Erro ao preparar requisição: ${e.message}"
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Função auxiliar para criar arquivo de imagem
private fun createImageFile(context: Context): File {
    val storageDir = context.cacheDir
    return File.createTempFile("IMG_", ".jpg", storageDir)
}
