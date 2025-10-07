package com.example.oportunyfam.Components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
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
data class CnpjState(
    val cnpj: String = "",
    val error: String? = null
)

// Constantes
private const val CNPJ_LENGTH = 14 // 14 dígitos
private const val MAX_INPUT_LENGTH = 18 // 14 dígitos + 4 caracteres de máscara

/**
 * VisualTransformation para aplicar a máscara 99.999.999/9999-99 no CNPJ.
 */
class CnpjMaskTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        // Limita e garante que apenas os 14 dígitos brutos sejam considerados
        val trimmed = if (text.text.length >= CNPJ_LENGTH) text.text.substring(0..CNPJ_LENGTH - 1) else text.text
        var out = ""

        // Adiciona a máscara
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 4) out += "."
            if (i == 7) out += "/"
            if (i == 11) out += "-"
        }

        val offsetTranslator = object : OffsetMapping {
            // Converte a posição do cursor (original -> com máscara)
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset + 1
                if (offset <= 8) return offset + 2
                if (offset <= 12) return offset + 3
                if (offset <= 14) return offset + 4
                return MAX_INPUT_LENGTH
            }

            // Converte a posição do cursor (com máscara -> original)
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 6) return offset - 1
                if (offset <= 10) return offset - 2
                if (offset <= 15) return offset - 3
                if (offset <= 18) return offset - 4
                return CNPJ_LENGTH
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
 * 2. COMPONENTE OutlinedTextField PARA CNPJ
 * -------------------------------------------
 */

@Composable
fun CnpjTextField(
    modifier: Modifier = Modifier,
    // Função chamada após a validação bem-sucedida (dígitos)
    onValidationSuccess: (cnpj: String) -> Unit = {}
) {
    // Gerenciamento de estado local
    var state by remember { mutableStateOf(CnpjState()) }
    val focusManager = LocalFocusManager.current

    // Função para tratar a entrada (apenas números, limite de 14)
    val onValueChange: (String) -> Unit = { newText ->
        // Remove caracteres não numéricos e limita a 14 dígitos
        val digitsOnly = newText.filter { it.isDigit() }
        val limitedDigits = if (digitsOnly.length > CNPJ_LENGTH) digitsOnly.substring(0, CNPJ_LENGTH) else digitsOnly

        // Atualiza o estado e limpa o erro ao digitar
        state = state.copy(cnpj = limitedDigits, error = null)
    }

    // Função que será chamada no ImeAction.Done (tecla Enter/Concluir)
    val onImeActionDone: () -> Unit = {
        // 1. Verifica se os 14 dígitos foram digitados
        if (state.cnpj.length < CNPJ_LENGTH) {
            state = state.copy(error = "O CNPJ deve conter 14 dígitos.")
            return@onImeActionDone
        }

        // 2. Validação da lógica dos dígitos do CNPJ
        if (!CnpjValidator.isCnpjValid(state.cnpj)) {
            state = state.copy(error = "O CNPJ digitado é inválido (erro de cálculo).")
            return@onImeActionDone
        }

        // 3. Sucesso na validação (lógica de dígitos)
        onValidationSuccess(state.cnpj)

        // 4. Move o foco para o próximo campo
        focusManager.moveFocus(FocusDirection.Next)
    }

    OutlinedTextField(
        value = state.cnpj,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text("CNPJ") },
        // Cor da borda vermelha em caso de erro
        isError = state.error != null,
        supportingText = {
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(text = "Digite o CNPJ (apenas números)")
            }
        },
        leadingIcon = {
            Icon(Icons.Filled.Business, contentDescription = "Ícone CNPJ")
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
        visualTransformation = CnpjMaskTransformation()
    )
}

/**
 * -------------------------------------------
 * 3. VALIDADOR DE CNPJ (Lógica de Dígitos)
 * -------------------------------------------
 */

// Objeto para conter a lógica de validação do CNPJ
object CnpjValidator {

    // Função que implementa a lógica de validação dos dígitos verificadores
    fun isCnpjValid(cnpj: String): Boolean {
        if (cnpj.length != CNPJ_LENGTH) return false

        // 1. Verifica CPFs com todos os dígitos iguais
        if (cnpj.toSet().size == 1) return false

        try {
            val numbers = cnpj.map { it.toString().toInt() }.toIntArray()

            // Pesos para os cálculos (1º e 2º dígito verificador)
            val weights1 = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            val weights2 = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

            // Cálculo do primeiro dígito verificador (DV1)
            var sum1 = 0
            for (i in 0..11) {
                sum1 += numbers[i] * weights1[i]
            }
            var remainder1 = sum1 % 11
            val dv1 = if (remainder1 < 2) 0 else 11 - remainder1

            if (numbers[12] != dv1) return false // Compara DV1 calculado com o DV1 digitado

            // Cálculo do segundo dígito verificador (DV2)
            var sum2 = 0
            for (i in 0..12) {
                sum2 += numbers[i] * weights2[i]
            }
            var remainder2 = sum2 % 11
            val dv2 = if (remainder2 < 2) 0 else 11 - remainder2

            return numbers[13] == dv2 // Compara DV2 calculado com o DV2 digitado

        } catch (e: Exception) {
            return false
        }
    }
}