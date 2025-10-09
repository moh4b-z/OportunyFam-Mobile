package com.example.oportunyfam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.AuthDataStore
import androidx.compose.ui.text.input.VisualTransformation
import br.senai.sp.jandira.oportunyfam.service.RetrofitFactory
import com.example.Components.LoginContent
import com.example.Components.RegistroContent
import com.example.oportunyfam.model.Instituicao
import kotlinx.coroutines.launch
import com.example.oportunyfam.R

val PrimaryColor = Color(0xFFFFA500)
val BackgroundGray = Color(0xFFE0E0E0)

object NavRoutes {
    const val REGISTRO = "tela_registro"
    const val HOME = "tela_home"
}

@Composable
fun RegistroScreen(navController: NavHostController?) {

    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }

    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val selectedTypeIds = remember { mutableStateOf(emptyList<Int>()) }
    val selectedTypeNames = remember { mutableStateOf("") }
    val cnpj = remember { mutableStateOf("") }


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


    val isRegisterSelected = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(1) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val instituicaoService = remember { RetrofitFactory().getInstituicaoService() }


    val onAuthSuccess: (Instituicao) -> Unit = { instituicaoLogada ->
        scope.launch {
            authDataStore.saveInstituicao(instituicaoLogada)
            navController?.navigate(NavRoutes.HOME) {
                popUpTo(NavRoutes.REGISTRO) { inclusive = true }
            }
            isLoading.value = false
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = stringResource(R.string.desc_icon_name),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .padding(horizontal = 40.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterSelected.value) stringResource(R.string.title_register) else stringResource(R.string.title_welcome_back),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isRegisterSelected.value) stringResource(R.string.subtitle_register) else stringResource(R.string.subtitle_login),
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            BackgroundGray,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(if (!isRegisterSelected.value) Color.White else BackgroundGray)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isRegisterSelected.value = false
                                errorMessage.value = null
                                currentStep.value = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.button_login),
                            fontSize = 16.sp,
                            color = if (!isRegisterSelected.value) PrimaryColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(if (isRegisterSelected.value) Color.White else BackgroundGray)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isRegisterSelected.value = true
                                errorMessage.value = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.button_register),
                            fontSize = 16.sp,
                            color = if (isRegisterSelected.value) PrimaryColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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

                if (!isRegisterSelected.value) {
                    LoginContent(
                        navController = navController,
                        email = email,
                        senha = senha,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        instituicaoService = instituicaoService,
                        scope = scope,
                        onAuthSuccess = onAuthSuccess
                    )
                } else {
                    RegistroContent(
                        navController = navController,
                        // Passo 1
                        nome = nome,
                        email = email,
                        phone = phone,
                        selectedTypeIds = selectedTypeIds,
                        selectedTypeNames = selectedTypeNames,
                        cnpj = cnpj,
                        // Passo 2
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
                        // Controles
                        currentStep = currentStep,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        instituicaoService = instituicaoService,
                        scope = scope,
                        onAuthSuccess = onAuthSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun RegistroOutlinedTextField(
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

@Preview(showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    RegistroScreen(navController = null)
}