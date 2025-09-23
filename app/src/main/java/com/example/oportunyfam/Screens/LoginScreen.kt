package com.example.oportunyfam.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
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
fun LoginScreens(navController: NavHostController?) {

    val email = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) } // Estado do checkbox
    val isLoginSelected = remember { mutableStateOf(true) } // Controle de seleção de botões (Login/Registrar-se)

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
                .offset(y = 170.dp), // mesma função que o padding
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
                .fillMaxHeight(0.65f) // ocupação da tela
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

                //Login/Registre-se
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(25.dp)) // fundo cinza claro
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
                                if (isLoginSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable { isLoginSelected.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = if (isLoginSelected.value) Color.Black else Color.Gray,
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
                                if (!isLoginSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable { isLoginSelected.value = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registre-se",
                            fontSize = 16.sp,
                            color = if (!isLoginSelected.value) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Campos dinâmicos de acordo com a seleção (Login ou Registro)
                if (isLoginSelected.value) {
                    // Campos de login
                    // Email
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "",
                                tint = Color(0x9E000000)
                            )
                        },
                        placeholder = {
                            Text(text = "Digite seu email")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Senha
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
                            Text(text = "Digite sua senha")
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Checkbox e "Esqueceu sua senha?"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked.value,
                                onCheckedChange = { checked.value = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFFA500),
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lembrar de mim",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }

                        Text(
                            text = "Esqueceu sua senha?",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botão Login
                    Button(
                        onClick = {
                            navController?.navigate("HomeScreen")},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA500)
                        )
                    ) {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // Campos para o registro podem ser colocados aqui, como nome, telefone, etc.
                    Text(
                        text = "Aqui vão os campos para o registro",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
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
                        text = "Ou entre com",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Box criada para fazer o Botão Google
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        // fazer sombra do botão
                        .shadow(
                            elevation = 8.dp, // altura da sombra
                            shape = RoundedCornerShape(50) // borda arredondada
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
fun LoginScreensPreview() {
    LoginScreens(navController = null)
}


