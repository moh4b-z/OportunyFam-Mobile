package com.oportunyfam_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oportunyfam_mobile.Screens.ChatScreen
import com.oportunyfam_mobile.Screens.ConversasScreen
import com.oportunyfam_mobile.Screens.SplashScreen
import com.oportunyfam_mobile.Screens.RegistroScreen
import com.oportunyfam_mobile.Screens.HomeScreen
import com.oportunyfam_mobile.Screens.PerfilScreen
import com.oportunyfam_mobile.Screens.RegisterChildScreen
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
                    startDestination = NavRoutes.SPLASH
                ) {
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(navController)
                    }

                    // Tela de Registro/Login
                    composable(NavRoutes.REGISTRO) {
                        RegistroScreen(navController)
                    }

                    // Tela de Perfil
                    composable(NavRoutes.PERFIL) {
                        PerfilScreen(navController = navController)
                    }

                    // Tela Home
                    composable(NavRoutes.HOME) {
                        HomeScreen(navController)
                    }

                    // Tela de Registro de Filho
                    composable(NavRoutes.CHILD_REGISTER) {
                        RegisterChildScreen(navController = navController)
                    }

                    // Tela de Conversas
                    composable(NavRoutes.CONVERSAS) {
                        ConversasScreen(navController)
                    }
                    composable(
                        route = "${NavRoutes.CHAT}/{conversaId}/{nomeContato}/{pessoaIdAtual}",
                        arguments = listOf(
                            navArgument("conversaId") { type = NavType.IntType },
                            navArgument("nomeContato") { type = NavType.StringType },
                            navArgument("pessoaIdAtual") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val conversaId = backStackEntry.arguments?.getInt("conversaId") ?: 0
                        val nomeContato = backStackEntry.arguments?.getString("nomeContato") ?: ""
                        val pessoaIdAtual = backStackEntry.arguments?.getInt("pessoaIdAtual") ?: 0

                        ChatScreen(
                            navController = navController,
                            conversaId = conversaId,
                            nomeContato = nomeContato,
                            pessoaIdAtual = pessoaIdAtual
                        )
                    }
                }
            }
        }
    }

    companion object NavRoutes {
        const val SPLASH = "tela_splash"
        const val REGISTRO = "tela_registro"
        const val PERFIL = "tela_perfil"
        const val HOME = "HomeScreen"
        const val CHILD_REGISTER = "child_register"
        const val ATIVIDADES = "AtividadesScreen"
        const val CONVERSAS = "ConversasScreen"
        const val CHAT = "ChatScreen"
    }
}