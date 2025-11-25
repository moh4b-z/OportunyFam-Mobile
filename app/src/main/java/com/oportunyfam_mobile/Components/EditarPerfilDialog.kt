package com.oportunyfam_mobile.Components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.oportunyfam_mobile.Service.AzureBlobRetrofit
import com.oportunyfam_mobile.Service.UsuarioService
import com.oportunyfam_mobile.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val TAG = "EditarPerfilDialog"

@Composable
fun EditarPerfilDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onSave: (Usuario) -> Unit,
    usuarioService: UsuarioService,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    var nome by remember { mutableStateOf(usuario.nome) }
    var email by remember { mutableStateOf(usuario.email) }
    var telefone by remember { mutableStateOf(usuario.telefone ?: "") }
    var fotoUrl by remember { mutableStateOf(usuario.foto_perfil) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Launcher para seleção de imagem
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Upload imediatamente
            scope.launch {
                isUploading = true
                errorMessage = null
                try {
                    val uploadedUrl = uploadImageToAzure(context, uri)
                    if (uploadedUrl != null) {
                        fotoUrl = uploadedUrl
                        Log.d(TAG, "Imagem uploaded com sucesso: $uploadedUrl")
                    } else {
                        errorMessage = "Erro ao fazer upload da imagem"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro no upload: ${e.message}", e)
                    errorMessage = "Erro ao fazer upload: ${e.message}"
                }
                isUploading = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Editar Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA000)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        if (isUploading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFFFFA000))
                            }
                        } else if (!fotoUrl.isNullOrEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(context)
                                        .data(fotoUrl)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Sem foto",
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                    }

                    // Ícone de câmera sobreposto
                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFA000))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Alterar foto",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campos de edição
                RegistroOutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = "Nome",
                    readOnly = isSaving || isUploading
                )

                Spacer(modifier = Modifier.height(12.dp))

                RegistroOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    readOnly = isSaving || isUploading
                )

                Spacer(modifier = Modifier.height(12.dp))

                RegistroOutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = "Telefone",
                    readOnly = isSaving || isUploading
                )

                if (!errorMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && !isUploading
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            isSaving = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    // Buscar dados atuais
                                    val existingResp = withContext(Dispatchers.IO) {
                                        usuarioService.buscarPorId(usuario.usuario_id).execute()
                                    }

                                    if (existingResp.isSuccessful) {
                                        val existing = existingResp.body()?.usuario

                                        // Build map com campos alterados
                                        val fields = mutableMapOf<String, Any?>()
                                        if (nome.isNotBlank() && nome != existing?.nome) fields["nome"] = nome
                                        if (email.isNotBlank() && email != existing?.email) fields["email"] = email
                                        if (telefone.isNotBlank() && telefone != existing?.telefone) fields["telefone"] = telefone
                                        if (fotoUrl != existing?.foto_perfil) fields["foto_perfil"] = fotoUrl

                                        if (fields.isEmpty()) {
                                            isSaving = false
                                            errorMessage = "Nenhuma alteração detectada"
                                            return@launch
                                        }

                                        // Atualizar
                                        val updateResp = withContext(Dispatchers.IO) {
                                            usuarioService.atualizar(usuario.usuario_id, fields).execute()
                                        }

                                        if (updateResp.isSuccessful && updateResp.body()?.usuario != null) {
                                            isSaving = false
                                            onSave(updateResp.body()!!.usuario!!)
                                        } else {
                                            isSaving = false
                                            errorMessage = "Erro ao atualizar: ${updateResp.code()}"
                                        }
                                    } else {
                                        isSaving = false
                                        errorMessage = "Erro ao buscar dados: ${existingResp.code()}"
                                    }
                                } catch (e: Exception) {
                                    isSaving = false
                                    errorMessage = "Erro: ${e.message}"
                                    Log.e(TAG, "Erro ao salvar perfil", e)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun uploadImageToAzure(context: android.content.Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Criar arquivo temporário
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Configurações do Azure - usar o token correto
            val storageAccount = com.oportunyfam_mobile.Config.AzureConfig.STORAGE_ACCOUNT
            val sasToken = com.oportunyfam_mobile.Config.AzureConfig.getStorageKey()
                ?: "sp=racwl&st=2025-11-18T13:06:56Z&se=2025-12-05T21:21:56Z&sv=2024-11-04&sr=c&sig=blfBJt5Lw0S9tB1mSpo%2FRufvFq5eXaPQNFI3mZ36Z5Y%3D"
            val containerName = com.oportunyfam_mobile.Config.AzureConfig.CONTAINER_PERFIL

            Log.d(TAG, "📤 Usando storage account: $storageAccount, container: $containerName")

            // Upload
            val uploadedUrl = AzureBlobRetrofit.uploadImageToAzure(
                imageFile = tempFile,
                storageAccount = storageAccount,
                sasToken = sasToken,
                containerName = containerName
            )

            // Limpar arquivo temporário
            tempFile.delete()

            uploadedUrl
        } catch (e: Exception) {
            Log.e(TAG, "Erro no upload para Azure: ${e.message}", e)
            null
        }
    }
}
