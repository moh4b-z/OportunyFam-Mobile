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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.example.oportunyfam.R

// CORES

val LightPurple = Color(0xFFEFECFF)
val DarkGray = Color(0xFF333333)
val MediumGray = Color(0xFF666666)
val LightGray = Color(0xFF999999)

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
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        leadingIcon = leadingIcon.takeIf { it != {} },
        trailingIcon = trailingIcon,
        label = { Text(label, color = LightGray) },
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        enabled = !readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color(0xFFE5E5E5),
            disabledBorderColor = Color(0xFFE5E5E5),
            cursorColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = LightGray,
            focusedTextColor = DarkGray,
            unfocusedTextColor = DarkGray
        )
    )
}

// COMPONENTE DE BOTÃO DE LOGIN SOCIAL
@Composable
fun SocialLoginButton(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = DarkGray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = DarkGray)
        }
    }
}

// CONTEÚDO DA TELA DE LOGIN
@Composable
fun LoginContent(
    navController: NavHostController,
    email: String,
    onEmailChange: (String) -> Unit,
    senha: String,
    onSenhaChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Campo Email
        LoginOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            readOnly = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Senha
        LoginOutlinedTextField(
            value = senha,
            onValueChange = onSenhaChange,
            label = "Senha",
            readOnly = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lembrar de mim e Esqueci a senha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = PrimaryColor
                    )
                )
                Text(
                    text = "Lembrar de mim",
                    color = MediumGray,
                    fontSize = 14.sp
                )
            }

            Text(
                text = "Esqueci a senha?",
                color = PrimaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onForgotPasswordClick() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão de Login
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
        ) {
            Text("Login", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divisor "Ou login com"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFE5E5E5))
            )
            Text(
                text = "Ou login com",
                color = LightGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color(0xFFE5E5E5))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botões de Login Social
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SocialLoginButton(
                icon = R.drawable.google, // Adicione seus ícones
                text = "Google",
                onClick = { /* TODO: Google login */ }
            )

        }
    }
}

// TELA DE LOGIN COMPLETA
@Composable
fun LoginScreen(
    navController: NavHostController,
    onAuthSuccess: () -> Unit = {}
) {
    val isRegisterSelected = remember { mutableStateOf(false) }
    val email = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val nome = remember { mutableStateOf("") }
    val rememberMe = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de topo
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = "Login background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Box do conteúdo principal (título e subtítulo)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .padding(horizontal = 40.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (isRegisterSelected.value) "Criar Conta" else "Seja bem-vindo novamente",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegisterSelected.value) "Preencha seus dados" else "Guie em seu 80% para usar os nossos serviços.",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        // Card com o conteúdo do login
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
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
                        .background(LightPurple, RoundedCornerShape(25.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Login
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (!isRegisterSelected.value) PrimaryColor else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isRegisterSelected.value = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Login",
                            color = if (!isRegisterSelected.value) Color.White else MediumGray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Register
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (isRegisterSelected.value) PrimaryColor else Color.Transparent,
                                RoundedCornerShape(25.dp)
                            )
                            .clickable { isRegisterSelected.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Register",
                            color = if (isRegisterSelected.value) Color.White else MediumGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!isRegisterSelected.value) {
                    // Conteúdo Login
                    LoginContent(
                        navController = navController,
                        email = email.value,
                        onEmailChange = { email.value = it },
                        senha = senha.value,
                        onSenhaChange = { senha.value = it },
                        rememberMe = rememberMe.value,
                        onRememberMeChange = { rememberMe.value = it },
                        onLoginClick = { /* TODO: Implement login */ },
                        onForgotPasswordClick = { /* TODO: Navigate to forgot password */ }
                    )
                } else {
                    // Conteúdo Register
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Email as safras Pavilionas Trata novamente!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MediumGray,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        LoginOutlinedTextField(
                            value = nome.value,
                            onValueChange = { nome.value = it },
                            label = "Nome Completo",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginOutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            label = "Email",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LoginOutlinedTextField(
                            value = senha.value,
                            onValueChange = { senha.value = it },
                            label = "Senha",
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { /* TODO: Implement register */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("Registrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}