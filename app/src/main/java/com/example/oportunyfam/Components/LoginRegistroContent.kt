package com.example.oportunyfam.Components.Login

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.oportunyfam.R
import com.example.oportunyfam.Screens.BackgroundGray
import com.example.oportunyfam.Screens.PrimaryColor

@Composable
fun LoginRegistro(
    navController: NavController,
    isRegisterSelected: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    currentStep: MutableState<Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                BackgroundGray,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Botão Login ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(if (!isRegisterSelected.value) Color.White else BackgroundGray)
//                .clickable(
//                    //interactionSource = remember { MutableInteractionSource() },
//                    //indication = LocalIndication.current
//                ) {
//                    isRegisterSelected.value = false
//                    errorMessage.value = null
//                    currentStep.value = 1
//
//                    navController.navigate("tela_registro") {
//                        popUpTo("login_screen") { inclusive = true }
//                    }
//                }
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.button_login),
                fontSize = 16.sp,
                color = if (!isRegisterSelected.value) PrimaryColor else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }

        // --- Botão Registro ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(if (isRegisterSelected.value) Color.White else BackgroundGray)
//                .clickable(
//                    //interactionSource = remember { MutableInteractionSource() },
//                    //indication = LocalIndication.current
//                ) {
//                    errorMessage.value = null
//                    currentStep.value = 1
//
//                    navController.navigate("register_screen") {
//                        popUpTo("register_screen") { inclusive = true }
//                    }
//                }
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.button_register),
                fontSize = 16.sp,
                color = if (isRegisterSelected.value) PrimaryColor else Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginRegistroPreview() {
    val navController = rememberNavController() // ✅ NavController no Preview
    val isRegisterSelected = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val currentStep = remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        LoginRegistro(
            navController = navController,
            isRegisterSelected = isRegisterSelected,
            errorMessage = errorMessage,
            currentStep = currentStep
        )
    }
}
