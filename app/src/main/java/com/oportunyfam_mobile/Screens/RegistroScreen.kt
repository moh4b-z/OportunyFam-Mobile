package com.oportunyfam_mobile.Screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.oportunyfam_mobile.Service.RetrofitFactory
import com.oportunyfam_mobile.data.AuthDataStore
import com.oportunyfam_mobile.data.AuthType
import com.oportunyfam_mobile.Components.AuthContainer
import com.oportunyfam_mobile.Components.LoginContent
import com.oportunyfam_mobile.R
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.oportunyfam_mobile.Components.RegistroContent // Usando o nome RegistroContent
import com.oportunyfam_mobile.model.Usuario
import androidx.compose.foundation.layout.Column

@Composable
fun RegistroScreen(navController: NavHostController) {

    // --- Variáveis de Estado de Controle ---
    val isRegisterSelected = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }

    // --- Variáveis de Estado do Formulário de Registro (TODOS OS CAMPOS) ---
    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val dataNascimento = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val idSexo = remember { mutableStateOf<Int?>(null) }
    val selectedSexoName = remember { mutableStateOf("") }
    val cep = remember { mutableStateOf("") }
    val logradouro = remember { mutableStateOf("") }
    val numero = remember { mutableStateOf("") }
    val complemento = remember { mutableStateOf("") }
    val bairro = remember { mutableStateOf("") }
    val cidade = remember { mutableStateOf("") }
    val estado = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val concordaTermos = remember { mutableStateOf(false) }


    // Serviços
    val usuarioService = remember { RetrofitFactory().getUsuarioService() }
    val loginUniversalService = remember { RetrofitFactory().getLoginUniversalService() }
    val sexoService = remember { RetrofitFactory().getSexoService() }

    // --- Funções de Callback (Sucesso de Login) ---
    val onLoginSuccess: (destination: String) -> Unit = { destination ->
        scope.launch {
            navController.navigate(destination) {
                popUpTo("tela_registro") { inclusive = true }
            }
            isLoading.value = false
        }
    }

    // --- Funções de Callback (Sucesso de Registro) ---
    val onRegistrationSuccess: (Usuario) -> Unit = { usuario ->
        scope.launch {
            authDataStore.saveAuthUser(usuario, AuthType.USUARIO)
            navController.navigate("home") {
                popUpTo("tela_registro") { inclusive = true }
            }
            isLoading.value = false
        }
    }

    // Função para alternar entre Login e Registro
    val onToggle: (Boolean) -> Unit = { isRegister ->
        isRegisterSelected.value = isRegister
        errorMessage.value = null
        if (!isRegister) {
            nome.value = ""
            email.value = ""
            senha.value = ""
        }
    }

    // --- Determina Textos do Container ---
    val title = if (isRegisterSelected.value) stringResource(R.string.title_register) else stringResource(R.string.title_welcome_back)
    val subtitle = if (isRegisterSelected.value) stringResource(R.string.subtitle_register) else stringResource(R.string.subtitle_login)

    // --- Renderiza o Container ---
    AuthContainer(
        title = title,
        subtitle = subtitle,
        // CORREÇÃO MAIS SEGURA: Passar 'content' de forma EXPLICITA e NOMEADA
        content = {
            // O AuthContainer agora exibe o errorMessage, então a linha Text(it,...) foi removida
            // da lógica da tela para evitar duplicação, mas o conteúdo principal permanece.

            Column { // Mantemos a Column para estruturar o conteúdo
                if (isRegisterSelected.value) {
                    RegistroContent (
                        navController = navController,
                        nome = nome,
                        email = email,
                        phone = phone,
                        dataNascimento = dataNascimento,
                        cpf = cpf,
                        idSexo = idSexo,
                        selectedSexoName = selectedSexoName,
                        cep = cep,
                        logradouro = logradouro,
                        numero = numero,
                        complemento = complemento,
                        bairro = bairro,
                        cidade = cidade,
                        estado = estado,
                        senha = senha,
                        confirmarSenha = confirmarSenha,
                        concordaTermos = concordaTermos,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        usuarioService = usuarioService,
                        sexoService = sexoService,
                        scope = scope,
                        onAuthSuccess = onRegistrationSuccess
                    )
                } else {
                    LoginContent(
                        navController = navController,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        loginUniversalService = loginUniversalService,
                        authDataStore = authDataStore,
                        scope = scope,
                        onAuthSuccess = onLoginSuccess
                    )
                }
            }
        },
        isRegisterSelected = isRegisterSelected.value,
        onToggle = onToggle,
        errorMessage = errorMessage.value // O `errorMessage` é o último parâmetro
    )
}

@Preview(showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    RegistroScreen(navController = rememberNavController())
}