package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.Service.LoginUniversalService
import com.oportunyfam_mobile.model.ResultData // Importa a sealed class
import com.oportunyfam_mobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log // Importa Log para logging
import com.oportunyfam_mobile.model.LoginRequest

// Define a tag para o Logcat (ajuste conforme necessário)
private const val TAG = "LoginContent"

@Composable
fun LoginContent(
    navController: NavHostController,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    loginUniversalService: LoginUniversalService,
    authDataStore: AuthDataStore,
    scope: CoroutineScope,
    onAuthSuccess: (destination: String) -> Unit
) {
    val context = LocalContext.current
    // ESTADOS LOCAIS
    val email = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val rememberMe = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Email
        RegistroOutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = stringResource(R.string.label_email),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.desc_icon_email), tint = Color(0x9E000000)) },
            readOnly = isLoading.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            // CORREÇÃO: Adicionando o parâmetro supportingText faltante
            supportingText = {}
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Senha
        RegistroOutlinedTextField(
            value = senha.value,
            onValueChange = { senha.value = it },
            label = stringResource(R.string.label_password),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
            visualTransformation = PasswordVisualTransformation(),
            readOnly = isLoading.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            // CORREÇÃO: Adicionando o parâmetro supportingText faltante
            supportingText = {}
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Lembrar-me + Esqueceu senha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe.value,
                    onCheckedChange = { rememberMe.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor),
                    enabled = !isLoading.value
                )
                Text(stringResource(R.string.label_remember_me), color = Color.Gray, fontSize = 14.sp)
            }
            TextButton(
                onClick = { /* TODO: Implementar navegação para 'esqueceu senha' */ },
                enabled = !isLoading.value
            ) {
                Text(stringResource(R.string.button_forgot_password), color = PrimaryColor, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Botão Login
        Button(
            onClick = {
                errorMessage.value = null

                if (email.value.isBlank() || senha.value.isBlank()) {
                    errorMessage.value = context.getString(R.string.error_fill_all_login)
                    return@Button
                }

                isLoading.value = true

                scope.launch {
                    try {
                        val request = LoginRequest(
                            email = email.value,
                            senha = senha.value
                        )

                        val response = loginUniversalService.loginUniversal(request)

                        if (response.isSuccessful) {
                            val resultLogin = response.body()

                            if (resultLogin != null) {

                                // ** LÓGICA DE NAVEGAÇÃO E AUTENTICAÇÃO USANDO SEALED CLASS **
                                when (val resultData = resultLogin.result) {
                                    is ResultData.UsuarioResult -> {
                                        authDataStore.saveAuthUser(resultData.data, AuthType.USUARIO)
                                        // O login deu certo, navega para a home
                                        onAuthSuccess("tela_home")
                                    }
                                    is ResultData.CriancaData -> {
                                        authDataStore.saveAuthUser(resultData.crianca, AuthType.CRIANCA)
                                        // O login deu certo, navega para a home
                                        onAuthSuccess("tela_home")
                                    }
                                    is ResultData.InstituicaoResult -> {
                                        // Bloqueia se for uma Instituição logando no App de Família
                                        errorMessage.value = context.getString(R.string.error_not_responsible_app)
                                    }
                                    null -> {
                                        // Nenhum resultado válido
                                        errorMessage.value = context.getString(R.string.error_login_failed)
                                    }
                                    else -> {
                                        // Caso inesperado
                                        errorMessage.value = context.getString(R.string.error_login_failed)
                                    }
                                }

                            } else {
                                errorMessage.value = context.getString(R.string.error_login_failed)
                            }

                        } else if (response.code() == 401) {
                            errorMessage.value = context.getString(R.string.error_login_invalid_credentials)
                        } else {
                            // Erros HTTP não planejados (diferentes de 401)
                            val errorBody = response.errorBody()?.string() ?: response.message()
                            // ** REGRA: Loga o detalhe no Logcat, mostra apenas mensagem genérica para o usuário **
                            Log.e(TAG, "Erro HTTP inesperado (${response.code()}): $errorBody")
                            errorMessage.value = context.getString(R.string.error_login_failed)
                        }

                    } catch (e: Exception) {
                        // Erro de conexão, parse JSON (erro da sealed class), etc.
                        // ** REGRA: Loga o detalhe no Logcat, mostra apenas mensagem genérica para o usuário **
                        Log.e(TAG, "Erro de Conexão/Deserialização: ${e.message}", e)
                        errorMessage.value = context.getString(R.string.error_connection_failed)
                    } finally {
                        isLoading.value = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(25.dp),
            enabled = !isLoading.value
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(stringResource(R.string.button_login_submit).uppercase(), color = Color.White, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(15.dp))
    }
}
