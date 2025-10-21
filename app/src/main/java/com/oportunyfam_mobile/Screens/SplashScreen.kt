package com.oportunyfam_mobile.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.oportunyfam_mobile.data.AuthDataStore
import kotlinx.coroutines.delay
import com.oportunyfam_mobile.R

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    // Instância do AuthDataStore. O remember garante que ele só é criado uma vez.
    val authDataStore = remember { AuthDataStore(context) }

    SplashScreenContent(
        authDataStore = authDataStore,
        navController = navController
    )
}

@Composable
private fun SplashScreenContent(
    authDataStore: AuthDataStore,
    navController: NavController
) {
    val scale = remember { Animatable(0f) }

    // O LaunchedEffect gerencia a animação e a lógica de navegação
    LaunchedEffect(Unit) {
        // 1. Executa a animação
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
        delay(1000) // Tempo extra para o usuário ver a tela

        // 2. Verifica o estado de login no banco de dados
        val isLoggedIn = authDataStore.isUserLoggedIn()

        // 3. Navega com base no resultado e limpa o back stack
        if (isLoggedIn) {
            navController.navigate("tela_home") {
                popUpTo("SplashScreen") { inclusive = true }
            }
        } else {
            navController.navigate("tela_registro") {
                popUpTo("SplashScreen") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Cor de fundo para a tela de splash
            .background(Color(0xA9F69508)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo OportunyFam",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }
}

// O Preview agora usa um NavController mockado para evitar o erro "LayoutNode should be attached"
@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    val context = LocalContext.current
    val mockAuthDataStore = remember { AuthDataStore(context) }

    SplashScreenContent(
        authDataStore = mockAuthDataStore,
        navController = rememberNavController() // Fornece um NavController válido para o Preview
    )
}