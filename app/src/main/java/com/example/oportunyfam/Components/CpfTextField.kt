package com.example.oportunyfam.Components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

data class CpfState(
    val cpf: String = "",
    val error: String? = null
)

private const val CPF_LENGTH = 11
private const val MAX_INPUT_LENGTH = 14

class CpfMaskTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= CPF_LENGTH) text.text.substring(0..CPF_LENGTH - 1) else text.text
        var out = ""

        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 5) out += "."
            if (i == 8) out += "-"
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 6) return offset + 1
                if (offset <= 9) return offset + 2
                if (offset <= 11) return offset + 3
                return MAX_INPUT_LENGTH
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 11) return offset - 2
                if (offset <= 14) return offset - 3
                return CPF_LENGTH
            }
        }

        return TransformedText(
            text = AnnotatedString(out),
            offsetMapping = offsetTranslator
        )
    }
}

@Composable
fun CpfTextField(
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onValidationSuccess: (cpf: String) -> Unit = {}
) {
    var state by remember { mutableStateOf(CpfState()) }
    val focusManager = LocalFocusManager.current

    val onValueChange: (String) -> Unit = { newText ->
        val digitsOnly = newText.filter { it.isDigit() }
        val limitedDigits = if (digitsOnly.length > CPF_LENGTH) digitsOnly.substring(0, CPF_LENGTH) else digitsOnly
        state = state.copy(cpf = limitedDigits, error = null)
    }

    val onImeActionDone: () -> Unit = onImeActionDone@{
        if (state.cpf.length < CPF_LENGTH) {
            state = state.copy(error = "O CPF deve conter 11 dígitos.")
            return@onImeActionDone
        }

        if (!CpfValidator.isCpfValid(state.cpf)) {
            state = state.copy(error = "O CPF digitado é inválido (erro de cálculo).")
            return@onImeActionDone
        }

        onValidationSuccess(state.cpf)
        focusManager.moveFocus(FocusDirection.Next)
    }

    OutlinedTextField(
        value = state.cpf,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("CPF") },
        readOnly = readOnly,
        isError = state.error != null,
        supportingText = {
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(text = "Digite o CPF (apenas números)")
            }
        },
        leadingIcon = {
            Icon(Icons.Filled.Badge, contentDescription = "Ícone CPF")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeActionDone() }
        ),
        visualTransformation = CpfMaskTransformation()
    )
}

object CpfValidator {
    fun isCpfValid(cpf: String): Boolean {
        if (cpf.length != CPF_LENGTH) return false
        if (cpf.toSet().size == 1) return false

        try {
            val numbers = cpf.map { it.toString().toInt() }.toIntArray()

            var sum = 0
            for (i in 0..8) {
                sum += numbers[i] * (10 - i)
            }
            var remainder = sum % 11
            val dv1 = if (remainder < 2) 0 else 11 - remainder

            if (numbers[9] != dv1) return false

            sum = 0
            for (i in 0..9) {
                sum += numbers[i] * (11 - i)
            }
            remainder = sum % 11
            val dv2 = if (remainder < 2) 0 else 11 - remainder

            return numbers[10] == dv2
        } catch (e: Exception) {
            return false
        }
    }
}
