package com.oportunyfam_mobile.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.Components.ChildRegistration
import com.oportunyfam_mobile.Components.ChildRegistrationForm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterChildScreen(
    modifier: Modifier = Modifier,
    onSubmit: (ChildRegistration) -> Unit = {},
    navController: NavHostController? = null
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController?.navigate("HomeScreen") }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color(0xFF424242)
                    )
                }

                Text(
                    text = "Cadastrar Filho",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            ChildRegistrationForm(
                onSubmit = onSubmit,
                modifier = Modifier.fillMaxSize(),
                navController = navController
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRegisterChild() {
    MaterialTheme {
        RegisterChildScreen(onSubmit = { /*preview*/ })
    }
}
