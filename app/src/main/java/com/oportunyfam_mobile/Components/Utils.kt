package com.oportunyfam_mobile.Components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

val PrimaryColor = Color(0xFFFFA500)
val BackgroundGray = Color(0xFFE0E0E0)
const val CPF_LENGTH = 11
const val CEP_LENGTH = 8

// =================================================================
// FUNÇÕES DE VALIDAÇÃO
// =================================================================

fun isStep1Valid(nome: String, email: String, phone: String, cpf: String, dataNascimento: String, selectedSexoId: Int?): Boolean {
    val isBasicValid = nome.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
    val isCpfValid = cpf.length == CPF_LENGTH
    val isDateFilled = dataNascimento.length == 10 && dataNascimento.count { it == '/' } == 2
    val isSexoSelected = selectedSexoId != null && selectedSexoId > 0

    return isBasicValid && isCpfValid && isDateFilled && isSexoSelected
}

fun isStep2Valid(
    logradouro: String, bairro: String, cidade: String, estado: String,
    senha: String, confirmarSenha: String, concordaTermos: Boolean
): Boolean {
    val isAddressValid = logradouro.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()
    val isPasswordValid = senha.isNotBlank() && senha.length >= 6 && confirmarSenha.isNotBlank() && senha == confirmarSenha
    return isAddressValid && isPasswordValid && concordaTermos
}


// =================================================================
// COMPONENTE REUTILIZÁVEL DE INPUT
// =================================================================
@Composable
fun RegistroOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable() (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    supportingText: @Composable () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        leadingIcon = leadingIcon.takeIf { it != {} },
        trailingIcon = trailingIcon,
        label = { Text(label, color = Color.Gray) },
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = !readOnly,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray,
            disabledBorderColor = Color.LightGray,
            cursorColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = Color.Gray
        )
    )
}
