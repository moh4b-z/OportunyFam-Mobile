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
import com.oportunyfam_mobile.Screens.PerfilOngScreen
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
                    // Tela de Perfil da ONG/Instituição
                    composable(
                        route = "instituicao_perfil/{instituicao_id}",
                        arguments = listOf(
                            navArgument("instituicao_id") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val instituicaoId = backStackEntry.arguments?.getInt("instituicao_id") ?: 0
                        PerfilOngScreen(navController = navController, instituicaoId = instituicaoId)
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
                    composable(
                        route = "search_results/{query}",
                        arguments = listOf(
                            navArgument("query") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val query = backStackEntry.arguments?.getString("query") ?: ""
                        com.oportunyfam_mobile.Screens.SearchResultsScreen(
                            navController = navController,
                            initialQuery = query
                        )
                    }
                }
            }
        }
    }

    companion object NavRoutes {
        // Telas iniciais
        const val SPLASH = "tela_splash"
        const val REGISTRO = "tela_registro"

        // Telas principais
        const val HOME = "HomeScreen"
        const val PERFIL = "tela_perfil"
        const val PERFIL_ONG = "instituicao_perfil" // Perfil de instituições

        // Telas de cadastro
        const val CHILD_REGISTER = "child_register"

        // Telas de busca
        const val SEARCH_RESULTS = "search_results"

        // Telas de comunicação
        const val CONVERSAS = "ConversasScreen"
        const val CHAT = "ChatScreen"

        // Outras telas
        const val ATIVIDADES = "AtividadesScreen"
    }
}