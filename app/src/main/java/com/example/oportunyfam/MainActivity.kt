package com.example.oportunyfam


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oportunyfam.Screens.RegistroScreen
import com.example.oportunyfam.ui.theme.OportunyFamTheme
import com.example.tcc.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OportunyFamTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "tela_registro"
                ) {
                    composable( "tela_registro") {
                        RegistroScreen(navController)
                    }
                    composable ("tela_home"){
                        HomeScreen(navController)

                    }
                }
            }
        }
    }
}




