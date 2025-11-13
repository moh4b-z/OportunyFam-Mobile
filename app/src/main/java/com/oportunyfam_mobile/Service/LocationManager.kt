package com.oportunyfam_mobile.Service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Gerenciador de localização do usuário
 */
class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val context = context

    /**
     * Obtém a última localização conhecida do usuário
     */
    fun getCurrentLocation(onLocationReceived: (Location?) -> Unit) {
        try {
            // Verifica permissões
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    onLocationReceived(location)
                }.addOnFailureListener {
                    onLocationReceived(null)
                }
            } else {
                onLocationReceived(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onLocationReceived(null)
        }
    }
}

