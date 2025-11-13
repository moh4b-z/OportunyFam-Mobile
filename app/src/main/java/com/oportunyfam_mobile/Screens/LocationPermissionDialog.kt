package com.oportunyfam_mobile.Screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

/**
 * Verifica se a permissão de localização foi concedida
 */
fun hasLocationPermission(context: Context): Boolean {
    val fineLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocation = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocation && coarseLocation
}

/**
 * Abre as configurações de localização do dispositivo
 */
fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    context.startActivity(intent)
}

/**
 * Diálogo para solicitar ativação de localização
 */
@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    context: Context,
    onLocationPermissionGranted: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ativar Localização",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF69508)
            )
        },
        text = {
            Text(
                "Para oferecer a melhor experiência e encontrar ONGs próximas a você, precisamos acessar sua localização.\n\nVocê pode ativar isso nas configurações do seu dispositivo.",
                fontSize = 16.sp,
                color = Color.Gray
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    openLocationSettings(context)
                    onLocationPermissionGranted?.invoke()
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF69508)
                )
            ) {
                Text(
                    "Ativar Localização",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray
                )
            ) {
                Text(
                    "Agora Não",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

