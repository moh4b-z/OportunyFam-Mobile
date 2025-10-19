package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.oportunyfam_mobile.Service.SexoService
import com.oportunyfam_mobile.model.Sexo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // ✨ Adiciona o OptIn para componentes experimentais
@Composable
fun SexoSelector(
    selectedSexoId: MutableState<Int?>,
    selectedSexoName: MutableState<String>,
    isEnabled: Boolean,
    sexoService: SexoService,
    scope: CoroutineScope
) {
    var expanded by remember { mutableStateOf(false) }
    var sexosList by remember { mutableStateOf(emptyList<Sexo>()) }

    // Efeito para carregar a lista de sexos apenas uma vez
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Executar a chamada de forma síncrona dentro da coroutine
                val response = sexoService.listarTodos().execute()
                if (response.isSuccessful && response.body()?.sexos != null) {
                    sexosList = response.body()!!.sexos
                }
            } catch (e: Exception) {
                // Em um app real, aqui você mostraria uma mensagem de erro na UI
                println("Erro ao carregar sexos: ${e.message}")
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && isEnabled },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSexoName.value.ifBlank { "Selecione o Gênero" },
            onValueChange = {},
            readOnly = true,
            label = { Text("Gênero (Sexo)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            enabled = isEnabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sexosList.forEach { sexo ->
                DropdownMenuItem(
                    text = { Text(sexo.nome) },
                    onClick = {
                        selectedSexoId.value = sexo.id
                        selectedSexoName.value = sexo.nome
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}