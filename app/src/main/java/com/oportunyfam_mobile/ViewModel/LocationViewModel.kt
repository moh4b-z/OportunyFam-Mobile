package com.oportunyfam_mobile.ViewModel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.oportunyfam_mobile.Service.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserLocationState(
    val userLocation: LatLng? = null,
    val isLocationEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LocationViewModel(context: Context) : ViewModel() {
    private val locationManager = LocationManager(context)

    private val _locationState = MutableStateFlow(UserLocationState())
    val locationState: StateFlow<UserLocationState> = _locationState.asStateFlow()

    /**
     * Solicita a localização atual do usuário
     */
    fun fetchUserLocation() {
        _locationState.value = _locationState.value.copy(isLoading = true)

        locationManager.getCurrentLocation { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                _locationState.value = UserLocationState(
                    userLocation = userLatLng,
                    isLocationEnabled = true,
                    isLoading = false,
                    error = null
                )
            } else {
                _locationState.value = _locationState.value.copy(
                    isLoading = false,
                    error = "Não foi possível obter a localização"
                )
            }
        }
    }

    /**
     * Marca que o usuário habilitou a localização
     */
    fun setLocationEnabled() {
        _locationState.value = _locationState.value.copy(isLocationEnabled = true)
        fetchUserLocation()
    }

    /**
     * Reseta o estado de localização
     */
    fun resetLocation() {
        _locationState.value = UserLocationState()
    }
}

