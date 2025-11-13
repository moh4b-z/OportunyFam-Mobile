package com.oportunyfam_mobile.Screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
        // Efeito bounce suave do logo
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        scale.animateTo(1f, animationSpec = tween(300))

        // Fade do fundo
        alpha.animateTo(1f, animationSpec = tween(1500))
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

    // Fundo gradiente mais quente e moderno
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF69508), // Laranja
                        Color(0xFFFFC14D), // Amarelo
                        Color(0xFFFFF1D2)  // Suave creme
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bolinhas animadas no fundo
        ParticlesLayer(alpha = alpha.value)

        // Logo animada
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo OportunyFam",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        )
    }
}

/**
 * Camada de partículas animadas — agora com movimento mais rápido
 */
@Composable
fun ParticlesLayer(alpha: Float) {
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextInt(4, 10),
                delay = Random.nextLong(0, 1000),
                speed = Random.nextFloat() * 0.005f + 0.0025f // 👈 mais rápido
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            var offsetY by remember { mutableStateOf(particle.y) }
            var rotation by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                delay(particle.delay)
                while (true) {
                    offsetY -= particle.speed
                    rotation += 2.5f // 👈 rotação mais perceptível
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
                        color = Color.White.copy(alpha = alpha * 0.35f),
                        shape = CircleShape
                    )
                    .alpha(alpha)
            )
        }
    }
}

data class Particle(
    val x: Float,
    var y: Float,
    val size: Int,
    val delay: Long,
    val speed: Float
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
