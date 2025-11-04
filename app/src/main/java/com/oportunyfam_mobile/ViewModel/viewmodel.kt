package com.oportunyfam_mobile.ViewModel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class EnderecoViewModel : ViewModel() {
    val endereco = mutableStateOf("")        // String
    val houseNumber = mutableStateOf("")     // String
    val road = mutableStateOf("")            // String
    val city = mutableStateOf("")            // String
    val displayName = mutableStateOf("")     // String

    fun atualizarEndereco(house: String, roadName: String, cityName: String, display: String) {
        houseNumber.value = house
        road.value = roadName
        city.value = cityName
        displayName.value = display
        endereco.value = "$roadName, $house"
    }
}