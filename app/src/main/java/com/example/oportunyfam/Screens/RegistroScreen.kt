package com.example.oportunyfam.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.R
import java.util.Calendar

@Composable
fun RegistroScreen(navController: NavHostController?) {

    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val number = remember { mutableStateOf("") }
    val data = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val cep = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val isRegisterSelected = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(1) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados de erro por campo
    var nomeError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var senhaError by remember { mutableStateOf<String?>(null) }
    var confirmSenhaError by remember { mutableStateOf<String?>(null) }
    var dataError by remember { mutableStateOf<String?>(null) }
    var cadastroError by remember { mutableStateOf<String?>(null) }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val context = LocalContext.current
    var dataNascimento by remember { mutableStateOf("") }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dataNascimento = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            dataError = null
        },
        year,
        month,
        day
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem topo
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Card sobre a imagem
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(120.dp)
                .offset(y = 140.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x8CFFA500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Crie sua conta e junte-se a nós!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estamos felizes em ter você por aqui!",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        // Card principal
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

                // Botões Login / Registro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botão Login
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (!isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) {
                                isRegisterSelected.value = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Registro
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) {
                                isRegisterSelected.value = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registre-se",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Condicional de cadastro
                if (isRegisterSelected.value) {

                    // Passo 1
                    if (currentStep.value == 1) {
                        OutlinedTextField(
                            value = nome.value,
                            onValueChange = { nome.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Nome") }
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        OutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Digite o email do responsável") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = number.value,
                            onValueChange = { number.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.AddIcCall, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Contato do Responsável") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Data de nascimento
                        Text(text = "Data de nascimento", fontSize = 12.sp)
                        OutlinedTextField(
                            value = dataNascimento,
                            onValueChange = {
                                dataNascimento = it
                                dataError = null
                            },
                            label = { Text("Data de nascimento") },
                            placeholder = { Text("DD/MM/AAAA") },
                            leadingIcon = {
                                IconButton(onClick = { datePickerDialog.show() }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = dataError != null
                        )

                        dataError?.let {
                            Text(it, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) {
                                    currentStep.value = 2
                                },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prosseguir",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFA500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "", tint = Color(0xFFFFA500))
                        }
                    }

                    // Passo 2
                    if (currentStep.value == 2) {
                        OutlinedTextField(
                            value = cpf.value,
                            onValueChange = { cpf.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("CPF") }
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        OutlinedTextField(
                            value = cep.value,
                            onValueChange = { cep.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("CEP") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Senha
                        OutlinedTextField(
                            value = senha.value,
                            onValueChange = {
                                senha.value = it
                                senhaError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                val visibilityIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(visibilityIcon, contentDescription = null)
                                }
                            },
                            label = { Text("Digite sua senha") },
                            placeholder = { Text(stringResource(R.string.senha_digitar)) },
                            singleLine = true,
                            isError = senhaError != null
                        )
                        senhaError?.let {
                            Text(it, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Confirmar Senha
                        Text(text = "Confirmar senha", fontSize = 12.sp)
                        OutlinedTextField(
                            value = confirmarSenha.value,
                            onValueChange = { confirmarSenha.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            label = { Text("Confirme sua senha") },
                            placeholder = { Text("Digite novamente a senha") },
                            singleLine = true,
                            isError = confirmSenhaError != null
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Button(
                            onClick = {
                                //criando validação
                                var valid = true

                                    if (nome.value.length < 3) {
                                        nomeError = context.getString(R.string.support_name)
                                        valid = false
                                    }

                                    if (!email.value.contains("@") || email.value.length < 5) {
                                        emailError = "Email inválido"
                                        valid = false
                                    }
                                    if (senha.value.length < 6) {
                                        senhaError = "A senha deve ter no mínimo 6 caracteres"
                                        valid = false
                                    }
                                    if (confirmarSenha.value != senha.value) {
                                        confirmSenhaError = "As senhas não coincidem"
                                        valid = false
                                    }

                                    if (dataNascimento.isBlank()) {
                                        dataError = "Escolha uma data"
                                        valid = false
                                    }
                                navController?.navigate("tela_home")

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) {
                            Text(
                                "Cadastrar",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Divider "Ou login com"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
                            Text(
                                "Ou entre com",
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // Botão Google
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.White)
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = LocalIndication.current
                                ) {  },//fazer a acao do google
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.google),
                                    contentDescription = "",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    RegistroScreen(navController = null)
}
