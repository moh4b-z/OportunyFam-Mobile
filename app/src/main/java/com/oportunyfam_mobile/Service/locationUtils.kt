package com.oportunyfam_mobile.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationUtils(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Retorna a última localização conhecida do usuário (ou null se não tiver permissão).
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): LatLng? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    cont.resume(LatLng(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }
}
