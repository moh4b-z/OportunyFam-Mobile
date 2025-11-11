package com.oportunyfam_mobile.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
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
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
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
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo zoom suave
        scale.animateTo(1f, animationSpec = tween(1500))
        // Brilho de fundo aparece aos poucos
        alpha.animateTo(1f, animationSpec = tween(2000))
        delay(1000)

        val isLoggedIn = authDataStore.isUserLoggedIn()

        if (isLoggedIn) {
            navController.navigate("HomeScreen") {
                popUpTo("tela_splash") { inclusive = true }
            }
        } else {
            navController.navigate("tela_registro") {
                popUpTo("tela_splash") { inclusive = true }
            }
        }
    }

    // Fundo com gradiente moderno
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF69508),      // Laranja principal
                        Color(0xFFFFD580),      // Amarelo claro
                        Color(0xFFFFFFFF)       // Branco
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Efeito de brilho/partículas leves de fundo
        ParticlesLayer(alpha = alpha.value)

        // Logo com animação de entrada
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo OportunyFam",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }
}

/**
 * Efeito sutil de partículas animadas (círculos pequenos subindo lentamente)
 */
@Composable
fun ParticlesLayer(alpha: Float) {
    val particles = remember {
        List(10) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextInt(4, 10),
                delay = Random.nextLong(0, 2000)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            var offsetY by remember { mutableStateOf(particle.y) }
            LaunchedEffect(Unit) {
                delay(particle.delay)
                while (true) {
                    offsetY -= 0.002f
                    if (offsetY < 0f) offsetY = 1f
                    delay(16)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .offset(
                        x = ((particle.x - 0.5f) * 300).dp,
                        y = ((offsetY - 0.5f) * 600).dp
                    )
                    .size(particle.size.dp)
                    .background(
                        color = Color.White.copy(alpha = alpha * 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

data class Particle(
    val x: Float,
    var y: Float,
    val size: Int,
    val delay: Long
)

@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    val context = LocalContext.current
    val mockAuthDataStore = remember { AuthDataStore(context) }

    SplashScreenContent(
        authDataStore = mockAuthDataStore,
        navController = rememberNavController()
    )
}
