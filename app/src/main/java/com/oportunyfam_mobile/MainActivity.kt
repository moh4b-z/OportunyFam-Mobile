package com.oportunyfam_mobile


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oportunyfam_mobile.Screens.ChatScreen
import com.oportunyfam_mobile.Screens.PerfilOngScreen
import com.oportunyfam_mobile.Screens.SplashScreen
import com.oportunyfam_mobile.Screens.RegistroScreen
import com.oportunyfam_mobile.Screens.HomeScreen
import com.oportunyfam_mobile.ui.theme.OportunyFamMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OportunyFamMobileTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "tela_splash"
                ) {
                    composable("tela_splash") {
                        SplashScreen(navController)
                    }

                    composable("tela_registro") {
                        RegistroScreen(navController)
                    }

                    composable("tela_home") {
                        HomeScreen(navController)
                    }

                    composable("tela_perfil") {
                        PerfilOngScreen(navController)

                    }
                    composable ("tela_chat"){
                        ChatScreen(navController)
                    }
                }
            }
        }
    }
}