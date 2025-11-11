package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.oportunyfam_mobile.model.Sexo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SexoSelector(
    selectedSexoId: MutableState<Int?>,
    selectedSexoName: MutableState<String>,
    isEnabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    // Lista hardcoded de sexos (valores comuns da API)
    val sexosList = remember {
        listOf(
            Sexo(id = 1, nome = "Masculino"),
            Sexo(id = 2, nome = "Feminino"),
            Sexo(id = 3, nome = "Outro"),
            Sexo(id = 4, nome = "Prefiro não informar")
        )
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