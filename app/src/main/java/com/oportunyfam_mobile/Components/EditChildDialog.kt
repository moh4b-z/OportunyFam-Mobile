package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.oportunyfam_mobile.model.Crianca
import com.oportunyfam_mobile.Service.RetrofitFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log

@Composable
fun EditChildDialog(
    crianca: Crianca?,
    onDismiss: () -> Unit,
    onSave: (Crianca) -> Unit
) {
    var nome by remember { mutableStateOf(crianca?.nome ?: "") }
    var email by remember { mutableStateOf(crianca?.email ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    if (crianca == null) { onDismiss(); return }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Editar Criança", fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                RegistroOutlinedTextField(value = nome, onValueChange = { nome = it }, label = "Nome", readOnly = isLoading)
                Spacer(modifier = Modifier.height(12.dp))
                RegistroOutlinedTextField(value = email, onValueChange = { email = it }, label = "Email", readOnly = isLoading)

                if (!errorMessage.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage!!, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val criancaService = RetrofitFactory().getCriancaService()
                                // Fetch existing
                                val existingResp = withContext(Dispatchers.IO) { criancaService.buscarPorId(crianca.crianca_id).execute() }
                                if (existingResp.isSuccessful) {
                                    val existing = existingResp.body()?.crianca

                                    // Build map of changed fields
                                    val fields = mutableMapOf<String, Any?>()
                                    if (nome.isNotBlank() && nome != existing?.nome) fields["nome"] = nome
                                    if (email.isNotBlank() && email != existing?.email) fields["email"] = email

                                    if (fields.isEmpty()) {
                                        isLoading = false
                                        errorMessage = "Nenhuma alteração detectada"
                                        return@launch
                                    }

                                    val updateResp = withContext(Dispatchers.IO) { criancaService.atualizar(crianca.crianca_id, fields).execute() }
                                    if (updateResp.isSuccessful && updateResp.body()?.crianca != null) {
                                        isLoading = false
                                        onSave(updateResp.body()!!.crianca!!)
                                    } else {
                                        isLoading = false
                                        errorMessage = "Erro ao atualizar criança: ${updateResp.code()}"
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Erro ao buscar dados da criança: ${existingResp.code()}"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Erro de conexão: ${e.message}"
                                Log.e("EditChildDialog", "Erro ao atualizar criança", e)
                            }
                        }
                    }, modifier = Modifier.weight(1f)) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp)) else Text("Salvar")
                    }
                }
            }
        }
    }
}
