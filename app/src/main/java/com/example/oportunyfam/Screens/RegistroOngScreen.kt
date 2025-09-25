package com.example.oportunyfam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.R

@Composable
fun RegistroOngScreens(navController: NavHostController?) {


    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val number = remember { mutableStateOf("") }
    val data = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val cep = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val isRegisterSelected = remember { mutableStateOf(true) }

    // aqui ele verifica em qual parte do cadastro o user esta
    val currentStep = remember { mutableStateOf(1) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            shape = RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp
            )
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
                        .background(Color(0xFFE0E0E0),
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
                            .background(
                                if (!isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable { isRegisterSelected.value = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable { isRegisterSelected.value = true },
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

                // condicional de cadastro
                if (isRegisterSelected.value) {

                    // cadastro
                    if (currentStep.value == 1) {
                        OutlinedTextField(
                            value = nome.value,
                            onValueChange = { nome.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
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
                            leadingIcon = {
                                Icon(Icons.Default.Email,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
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
                            leadingIcon = {
                                Icon(Icons.Default.AddIcCall,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
                            label = { Text("Contato do Responsável") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = data.value,
                            onValueChange = { data.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
                            label = { Text("00/00/0000") }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                //clickable faz passar os campos a ser preenchidos
                                .clickable {
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
                            Icon(Icons.Default.ArrowForward,
                                contentDescription = "",
                                tint = Color(0xFFFFA500))
                        }
                    }

                    // continuacao do cadastro
                    if (currentStep.value == 2) {
                        OutlinedTextField(
                            value = cpf.value,
                            onValueChange = { cpf.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Badge,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
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
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
                            label = { Text("CEP") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = senha.value,
                            onValueChange = { senha.value = it },
                            modifier = Modifier
                                .fillMaxWidth().
                                padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
                            label = { Text("Digite sua senha") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmarSenha.value,
                            onValueChange = { confirmarSenha.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(Icons.Default.Lock,
                                    contentDescription = "",
                                    tint = Color(0x9E000000))
                            },
                            label = { Text("Confirme sua senha") }
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) {
                            Text("Cadastrar",
                                color = Color.White,
                                fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier
                    .height(10.dp)
                )

                // Divider "Or login with"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
                    Text("Or login with", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier
                    .height(5.dp)
                )

                // Botão Google
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White,)
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RegistroOngScreensPreview() {
    RegistroOngScreens(navController = null)
}