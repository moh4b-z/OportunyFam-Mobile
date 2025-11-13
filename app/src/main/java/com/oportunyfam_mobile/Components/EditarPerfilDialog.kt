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
import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    // ...existing code...
    var fotoPerfil by remember { mutableStateOf(usuario.foto_perfil ?: "") }
    var telefone by remember { mutableStateOf(usuario.telefone ?: "") }
    var endereco by remember { mutableStateOf(usuario.cpf) } // Placeholder, precisa de campo real
    var statusOng by remember { mutableStateOf("Ativo") } // Placeholder

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para galeria de fotos
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            fotoPerfil = it.toString()
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

                // ===== SEÇÃO: ENDEREÇO =====
                Text(
                    text = "Endereço",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RegistroOutlinedTextField(
                    value = endereco,
                    onValueChange = { endereco = it },
                    label = "Endereço",
                    readOnly = isLoading,
                    supportingText = { Text("Endereço completo") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ===== SEÇÃO: STATUS ONG =====
                Text(
                    text = "Status da ONG",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var expandedStatus by remember { mutableStateOf(false) }
                val statusOptions = listOf("Ativo", "Inativo", "Pendente", "Bloqueado")

                Box(modifier = Modifier.fillMaxWidth()) {
                    RegistroOutlinedTextField(
                        value = statusOng,
                        onValueChange = {},
                        label = "Status",
                        readOnly = true,
                        supportingText = { Text("Selecione o status") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedStatus = !expandedStatus }
                    )

                    if (expandedStatus) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                statusOptions.forEach { status ->
                                    Text(
                                        text = status,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                statusOng = status
                                                expandedStatus = false
                                            }
                                            .padding(12.dp),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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

                            // Chamada à API para atualizar
                            scope.launch {
                                try {
                                    val request = UsuarioRequest(
                                        nome = usuario.nome,
                                        foto_perfil = fotoPerfil.takeIf { it.isNotEmpty() },
                                        email = usuario.email,
                                        senha = "", // Não alterar senha aqui
                                        data_nascimento = usuario.data_nascimento,
                                        telefone = telefone.filter { it.isDigit() },
                                        cpf = usuario.cpf,
                                        id_sexo = 0, // Ajustar conforme necessário
                                        id_tipo_nivel = 0, // Ajustar conforme necessário
                                        cep = "", // Ajustar conforme necessário
                                        logradouro = endereco,
                                        numero = "",
                                        complemento = null,
                                        bairro = "",
                                        cidade = "",
                                        estado = ""
                                    )

                                    // ALTERAÇÃO: Usar usuario.id (ID único) em vez de usuario_id (ID interno da tabela)
                                    // Isso garante que a requisição vá para PUT /v1/usuario/{id} com o ID correto
                                    usuarioService.atualizar(usuario.id, request)
                                        .enqueue(object : retrofit2.Callback<UsuarioResponse> {
                                            override fun onResponse(
                                                call: retrofit2.Call<UsuarioResponse>,
                                                response: retrofit2.Response<UsuarioResponse>
                                            ) {
                                                if (response.isSuccessful && response.body()?.usuario != null) {
                                                    val usuarioAtualizado = response.body()!!.usuario!!
                                                    isLoading = false
                                                    onSave(usuarioAtualizado)
                                                    onDismiss()
                                                } else {
                                                    errorMessage = "Erro ao atualizar perfil. Tente novamente."
                                                    isLoading = false
                                                }
                                            }

                                            override fun onFailure(
                                                call: retrofit2.Call<UsuarioResponse>,
                                                t: Throwable
                                            ) {
                                                errorMessage = "Erro de conexão: ${t.message}"
                                                isLoading = false
                                            }
                                        })
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

