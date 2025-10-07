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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.OffsetMapping

/**
 * -------------------------------------------
 * 1. CLASSE DE ESTADO E LÓGICA DE MÁSCARA
 * -------------------------------------------
 */

// Classe para gerenciar o estado do campo e as mensagens de erro
data class CpfState(
    val cpf: String = "",
    val error: String? = null
)

// Constantes
private const val CPF_LENGTH = 11 // 11 dígitos
private const val MAX_INPUT_LENGTH = 14 // 11 dígitos + 3 caracteres de máscara

/**
 * VisualTransformation para aplicar a máscara 999.999.999-99 no CPF.
 */
class CpfMaskTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        // Limita e garante que apenas os 11 dígitos brutos sejam considerados
        val trimmed = if (text.text.length >= CPF_LENGTH) text.text.substring(0..CPF_LENGTH - 1) else text.text
        var out = ""

        // Adiciona pontos e hífen
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 5) out += "."
            if (i == 8) out += "-"
        }

        val offsetTranslator = object : OffsetMapping {
            // Converte a posição do cursor (original -> com máscara)
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 6) return offset + 1
                if (offset <= 9) return offset + 2
                if (offset <= 11) return offset + 3
                return MAX_INPUT_LENGTH
            }

            // Converte a posição do cursor (com máscara -> original)
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset - 1
                if (offset <= 11) return offset - 2
                if (offset <= 14) return offset - 3
                return CPF_LENGTH
            }
        }

        return TransformedText(
            text = androidx.compose.ui.text.AnnotatedString(out),
            offsetMapping = offsetTranslator
        )
    }
}


/**
 * -------------------------------------------
 * 2. COMPONENTE OutlinedTextField PARA CPF
 * -------------------------------------------
 */

@Composable
fun CpfTextField(
    modifier: Modifier = Modifier,
    // Função chamada após a validação bem-sucedida (dígitos)
    onValidationSuccess: (cpf: String) -> Unit = {}
) {
    // Gerenciamento de estado local
    var state by remember { mutableStateOf(CpfState()) }
    val focusManager = LocalFocusManager.current

    // Função para tratar a entrada (apenas números, limite de 11)
    val onValueChange: (String) -> Unit = { newText ->
        // Remove caracteres não numéricos e limita a 11 dígitos
        val digitsOnly = newText.filter { it.isDigit() }
        val limitedDigits = if (digitsOnly.length > CPF_LENGTH) digitsOnly.substring(0, CPF_LENGTH) else digitsOnly

        // Atualiza o estado e limpa o erro ao digitar
        state = state.copy(cpf = limitedDigits, error = null)
    }

    // Função que será chamada no ImeAction.Done (tecla Enter/Concluir)
    val onImeActionDone: () -> Unit = {
        // 1. Verifica se os 11 dígitos foram digitados
        if (state.cpf.length < CPF_LENGTH) {
            state = state.copy(error = "O CPF deve conter 11 dígitos.")
            return@onImeActionDone
        }

        // 2. Validação da lógica dos dígitos do CPF
        if (!CpfValidator.isCpfValid(state.cpf)) {
            state = state.copy(error = "O CPF digitado é inválido (erro de cálculo).")
            return@onImeActionDone
        }

        // 3. Sucesso na validação (lógica de dígitos)
        onValidationSuccess(state.cpf)

        // 4. Move o foco para o próximo campo
        focusManager.moveFocus(FocusDirection.Next)
    }

    OutlinedTextField(
        value = state.cpf,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("CPF") },
        // Cor da borda vermelha em caso de erro
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
        // Apenas números
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done // Define a ação do teclado como "Done" (Concluir/Enter)
        ),
        // Ação de teclado (Enter) chama a função de validação
        keyboardActions = KeyboardActions(
            onDone = { onImeActionDone() }
        ),
        // Aplica a máscara visual
        visualTransformation = CpfMaskTransformation()
    )
}

/**
 * -------------------------------------------
 * 3. VALIDADOR DE CPF (Lógica de Dígitos)
 * -------------------------------------------
 */

// Objeto para conter a lógica de validação do CPF
object CpfValidator {

    // Função que implementa a lógica de validação dos dígitos verificadores
    fun isCpfValid(cpf: String): Boolean {
        // Garante que o input tem 11 dígitos
        if (cpf.length != CPF_LENGTH) return false

        // 1. Verifica CPFs com todos os dígitos iguais (ex: 11111111111)
        if (cpf.toSet().size == 1) return false

        try {
            // Converte a string de dígitos para um array de inteiros
            val numbers = cpf.map { it.toString().toInt() }.toIntArray()

            // 2. Cálculo do primeiro dígito verificador (DV1)
            var sum = 0
            for (i in 0..8) {
                sum += numbers[i] * (10 - i)
            }
            var remainder = sum % 11
            val dv1 = if (remainder < 2) 0 else 11 - remainder

            if (numbers[9] != dv1) return false // Compara DV1 calculado com o DV1 digitado

            // 3. Cálculo do segundo dígito verificador (DV2)
            sum = 0
            for (i in 0..9) {
                sum += numbers[i] * (11 - i)
            }
            remainder = sum % 11
            val dv2 = if (remainder < 2) 0 else 11 - remainder

            return numbers[10] == dv2 // Compara DV2 calculado com o DV2 digitado

        } catch (e: Exception) {
            // Caso ocorra algum erro inesperado na conversão ou cálculo
            return false
        }
    }
}