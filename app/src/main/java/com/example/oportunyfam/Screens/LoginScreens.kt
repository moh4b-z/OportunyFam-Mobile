package com.example.oportunyfam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import br.senai.sp.jandira.oportunyfam.service.RetrofitFactory
import com.example.Components.LoginContent
import com.example.data.AuthDataStore
import com.example.oportunyfam.R
import com.example.oportunyfam.model.Crianca
import com.example.oportunyfam.model.Usuario



// COMPONENTE PADRÃO DE CAMPO DE TEXTO DO LOGIN
@Composable
fun LoginOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable() (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    readOnly: Boolean,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        leadingIcon = leadingIcon.takeIf { it != {} },
        trailingIcon = trailingIcon,
        label = { Text(label, color = Color.Gray) },
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        enabled = !readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray,
            disabledBorderColor = Color.LightGray,
            cursorColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = Color.Gray
        )
    )
}

//TELA DE LOGIN
@Composable
fun LoginScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }

    // Estados do registro
    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val dataNascimento = remember { mutableStateOf("") }
    val selectedSexoId = remember { mutableStateOf<Int?>(null) }
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

    // Controle de tela
    val isRegisterSelected = remember { mutableStateOf(false) }
    val currentStep = remember { mutableStateOf(1) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Serviços
    val retrofitFactory = remember { RetrofitFactory() }
    val usuarioService = remember { retrofitFactory.getUsuarioService() }
    val sexoService = remember { retrofitFactory.getSexoService() }
    val loginUniversalService = remember { retrofitFactory.getLoginUniversalService() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de topo
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = stringResource(R.string.desc_icon_lock),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Box do conteúdo principal
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .padding(horizontal = 40.dp)
        ) {
            // Título
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isRegisterSelected.value)
                        stringResource(R.string.title_register)
                    else
                        stringResource(R.string.title_welcome_back),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // ADICIONANDO SUBTÍTULO (igual ao registro)
                Text(
                    text = if (isRegisterSelected.value)
                        stringResource(R.string.subtitle_register_user)
                    else
                        stringResource(R.string.subtitle_login),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        // Card com o conteúdo do login
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Toggle Login/Register
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(25.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isRegisterSelected.value) Color.White else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isRegisterSelected.value = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Login", fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isRegisterSelected.value) Color.White else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isRegisterSelected.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Register", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Mensagens de Erro ---
                errorMessage.value?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // --- Conteúdo Principal ---
                if (!isRegisterSelected.value) {
                    // Callback para login bem-sucedido
                    val onAuthSuccess: () -> Unit = {
                        navController?.navigate("home")
                    }

                    LoginContent(
                        navController = navController,
                        email = email,
                        senha = senha,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        loginUniversalService = loginUniversalService,
                        scope = scope,
                        onAuthSuccess = onAuthSuccess as (Usuario?, Crianca?) -> Unit
                    )
                } else {
                    // Conteúdo para Register
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Formulário de Registro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Campos do formulário de registro
                        LoginOutlinedTextField(
                            value = nome.value,
                            onValueChange = { nome.value = it },
                            label = "Nome Completo",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LoginOutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            label = "Email",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LoginOutlinedTextField(
                            value = senha.value,
                            onValueChange = { senha.value = it },
                            label = "Senha",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Lógica de registro aqui
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("Registrar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview(){
    LoginScreen(navController = null)
}