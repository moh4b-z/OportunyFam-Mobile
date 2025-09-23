package com.example.oportunyfam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
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
fun RegistroOngContiScreen(navController: NavHostController?) {

    val cpf = remember { mutableStateOf("") }
    val cep = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) } // Estado do checkbox
    val isRegisterSelected = remember { mutableStateOf(true) } // Controle de seleção de botões (Login/Registre-se)

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
                .fillMaxWidth(0.8f) // largura menor que a tela
                .height(120.dp)
                .offset(y = 140.dp), // mesma função que o padding
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x8CFFA500)) // cor com transparência
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

        // Card para preencher login
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f) // ocupação da tela
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

                //Login/Registre-se
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(25.dp))
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
                            .clickable { isRegisterSelected.value = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = if (!isRegisterSelected.value) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Registre-se
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
                            color = if (isRegisterSelected.value) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Condicional para mostrar os campos dependendo da seleção (Login ou Registro)
                if (isRegisterSelected.value) {
                    // Campos para o registro

                    //cpf
                    OutlinedTextField(
                        value = cpf.value,
                        onValueChange = { cpf.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "",
                                tint = Color(0x9E000000)
                            )
                        },
                        placeholder = {
                            Text(text = "CNPJ")
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // cep
                    OutlinedTextField(
                        value = cep.value,
                        onValueChange = { cep.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "",
                                tint = Color(0x9E000000)
                            )
                        },
                        placeholder = {
                            Text(text = "CEP")
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // senha
                    OutlinedTextField(
                        value = senha.value,
                        onValueChange = { senha.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "",
                                tint = Color(0x9E000000)
                            )
                        },
                        placeholder = {
                            Text(text = "Digite seu senha")
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // confirmarSenha ARRUMAR
                    OutlinedTextField(
                        value = confirmarSenha.value,
                        onValueChange = { confirmarSenha.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "",
                                tint = Color(0x9E000000)
                            )
                        },
                        placeholder = {
                            Text(text = "Confime sua senha ")
                        }
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    // Botão Prosseguir
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA500)
                        )
                    ) {
                        Text(
                            text = "Cadastrar",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Linha com texto no meio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Or login with",
                        color = Color.Gray,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                    )
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Box criada para fazer o Botao Google
                Box(
                    modifier = Modifier
                        .padding(10.dp)

                        // fazer sombra do botão
                        .shadow(
                            elevation = 8.dp, // altura da sombra
                            shape = RoundedCornerShape(50)
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(50)
                        )
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ){ }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "",
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Google",
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


@Preview(showSystemUi = true)
@Composable
fun RegistroOngContiScreenPreview() {
    RegistroOngContiScreen(navController = null)
}



