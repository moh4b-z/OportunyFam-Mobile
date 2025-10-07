package com.example.oportunyfam.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Constantes
private const val CEP_LENGTH = 8 // 8 dígitos
private const val MAX_INPUT_LENGTH = 9 // 8 dígitos + 1 caractere de máscara

// Classe de dados para a resposta do ViaCEP (atributos principais)
data class ViaCepData(
    val logradouro: String = "",
    val bairro: String = "",
    val localidade: String = "", // Cidade
    val uf: String = "", // Estado
    val erro: Boolean = false // Indica erro na API
)

// Classe de estado para o campo de CEP
data class CepState(
    val cep: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val data: ViaCepData? = null // Dados retornados pelo ViaCEP
)

/**
 * VisualTransformation para aplicar a máscara 99999-999 no CEP.
 */
class CepMaskTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        // Limita e garante que apenas os 8 dígitos brutos sejam considerados
        val trimmed = if (text.text.length >= CEP_LENGTH) text.text.substring(0..CEP_LENGTH - 1) else text.text
        var out = ""

        // Adiciona o hífen
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 4) out += "-"
        }

        val offsetTranslator = object : OffsetMapping {
            // Converte a posição do cursor (original -> com máscara)
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 5) return offset
                return offset + 1
            }

            // Converte a posição do cursor (com máscara -> original)
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 5) return offset
                return offset - 1
            }
        }

        return TransformedText(
            text = androidx.compose.ui.text.AnnotatedString(out),
            offsetMapping = offsetTranslator
        )
    }
}

@Composable
fun CepTextField(
    modifier: Modifier = Modifier,
    // Função chamada após a validação do CEP
    onValidationSuccess: (data: ViaCepData) -> Unit = {}
) {
    var state by remember { mutableStateOf(CepState()) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Armazena e permite a edição dos dados retornados pelo ViaCEP
    var logradouro by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var localidade by remember { mutableStateOf("") }
    var uf by remember { mutableStateOf("") }

    // Efeito para atualizar os campos editáveis quando o ViaCepData muda
    LaunchedEffect(state.data) {
        state.data?.let { data ->
            logradouro = data.logradouro
            bairro = data.bairro
            localidade = data.localidade
            uf = data.uf
        }
    }

    // Função para tratar a entrada (apenas números, limite de 8)
    val onValueChange: (String) -> Unit = { newText ->
        val digitsOnly = newText.filter { it.isDigit() }
        val limitedDigits = if (digitsOnly.length > CEP_LENGTH) digitsOnly.substring(0, CEP_LENGTH) else digitsOnly

        // Limpa o erro e os dados ao digitar
        state = state.copy(cep = limitedDigits, error = null, data = null)
    }

    // Função de validação e chamada à API (Enter/Done)
    val onImeActionDone: () -> Unit = onImeActionDone@{
        val cleanCep = state.cep

        // 1. Validação do formato (8 dígitos)
        if (cleanCep.length < CEP_LENGTH) {
            state = state.copy(error = "O CEP deve conter 8 dígitos.", data = null)
            return@onImeActionDone
        }

        // 2. Chamada à API ViaCEP
        state = state.copy(isLoading = true, error = null, data = null)
        coroutineScope.launch {
            val result = CepValidator.fetchViaCep(cleanCep)
            state = state.copy(isLoading = false)

            if (result.erro) {
                // Erro do ViaCEP (CEP não encontrado)
                state = state.copy(error = "CEP não encontrado. Digite o endereço manualmente ou verifique o número.", data = null)
            } else if (result.logradouro.isEmpty() && result.localidade.isEmpty()) {
                // Outro erro de formato/retorno vazio (para tratamento mais robusto)
                state = state.copy(error = "Falha ao buscar endereço, tente novamente.", data = null)
            } else {
                // Sucesso: armazena os dados, limpa o erro e move o foco
                state = state.copy(data = result)
                focusManager.moveFocus(FocusDirection.Down) // Move o foco para o próximo campo (Logradouro)
                onValidationSuccess(result)
            }
        }
    }

    Column(modifier = modifier) {
        // Campo principal de CEP
        OutlinedTextField(
            value = state.cep,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("CEP") },
            isError = state.error != null,
            trailingIcon = {
                if (state.isLoading) {
                    // Indicador de progresso durante a busca
                    CircularProgressIndicator(Modifier.size(20.dp))
                } else {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Ícone CEP")
                }
            },
            supportingText = {
                if (state.error != null) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                } else if (state.isLoading) {
                    Text(text = "Buscando endereço...")
                } else {
                    Text(text = "Digite o CEP (apenas números)")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeActionDone() }
            ),
            visualTransformation = CepMaskTransformation()
        )


        // Campos Adicionais do ViaCEP (Aparecem após a consulta)
        if (state.data != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Dados do Endereço", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))

            // Logradouro (Rua)
            OutlinedTextField(
                value = logradouro,
                onValueChange = { logradouro = it },
                label = { Text("Logradouro") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bairro e Localidade (Cidade) lado a lado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bairro,
                    onValueChange = { bairro = it },
                    label = { Text("Bairro") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = localidade,
                    onValueChange = { localidade = it },
                    label = { Text("Cidade") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // UF (Estado)
            OutlinedTextField(
                value = uf,
                onValueChange = { uf = it },
                label = { Text("UF") },
                modifier = Modifier.fillMaxWidth(0.3f), // Campo menor para UF
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                // Limita a entrada a 2 caracteres para UF
                supportingText = { if (uf.length > 2) uf = uf.substring(0, 2) },
            )
        }
    }
}

object CepValidator {

    /**
     * Simula a chamada à API ViaCEP.
     */
    suspend fun fetchViaCep(cep: String): ViaCepData = withContext(Dispatchers.IO) {
        // Remove a máscara
        val cleanCep = cep.replace("-", "")

        // Simulação de delay de rede
        kotlinx.coroutines.delay(1000)

        // SIMULAÇÃO DE RESPOSTAS COM BASE NO CEP:
        return@withContext when (cleanCep) {
            "01001000" -> ViaCepData(
                logradouro = "Praça da Sé",
                bairro = "Sé",
                localidade = "São Paulo",
                uf = "SP"
            )
            // Simulação de CEP válido, mas sem logradouro
            "01002000" -> ViaCepData(
                logradouro = "",
                bairro = "Centro",
                localidade = "São Paulo",
                uf = "SP"
            )
            // Simulação de erro
            "99999999" -> ViaCepData(erro = true)
            // Padrão para qualquer outro CEP
            else -> ViaCepData(
                logradouro = "Rua Exemplo, 123",
                bairro = "Bairro Teste",
                localidade = "Cidade Fictícia",
                uf = "RJ"
            )
        }
    }
}