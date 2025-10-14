package com.example.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.Service.UsuarioService
import com.example.oportunyfam.Service.SexoService
import com.example.oportunyfam.model.UsuarioRequest
import com.example.oportunyfam.model.Usuario
import com.example.oportunyfam.model.Sexo
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import com.example.oportunyfam.R
import com.example.data.AuthDataStore
import com.example.oportunyfam.model.UsuarioResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// =================================================================
// 1. COMPONENTE E VALIDADOR DE CPF (CÓDIGO FORNECIDO PELO USUÁRIO)
// =================================================================

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


// =================================================================
// 2. FUNÇÕES DE VALIDAÇÃO
// =================================================================

/**
 * Verifica se todos os campos obrigatórios do Passo 1 (Usuário) estão preenchidos.
 */
fun isStep1Valid(nome: String, email: String, phone: String, cpf: String, dataNascimento: String, selectedSexoId: Int?): Boolean {
    val isBasicValid = nome.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
    val isCpfValid = cpf.length == CPF_LENGTH
    val isDateValid = dataNascimento.isNotBlank() && dataNascimento.length == 10 // dd/MM/yyyy
    val isSexoSelected = selectedSexoId != null && selectedSexoId > 0

    return isBasicValid && isCpfValid && isDateValid && isSexoSelected
}

/**
 * Verifica se todos os campos obrigatórios (Endereço, Senhas e Termos) do Passo 2 estão válidos.
 */
fun isStep2Valid(
    logradouro: String, bairro: String, cidade: String, estado: String,
    senha: String, confirmarSenha: String, concordaTermos: Boolean
): Boolean {
    val isAddressValid = logradouro.isNotBlank() && bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()
    val isPasswordValid = senha.isNotBlank() && confirmarSenha.isNotBlank() && senha == confirmarSenha
    return isAddressValid && isPasswordValid && concordaTermos
}

// =================================================================
// 3. COMPONENTE AUXILIAR: Seleção de Sexo
// =================================================================

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


// =================================================================
// 4. COMPOSABLE PRINCIPAL (REGISTRO CONTENT)
// =================================================================

@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Passo 1 - Usuário (Responsável)
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    cpf: MutableState<String>, // NOVO: CPF
    dataNascimento: MutableState<String>, // NOVO: Data de Nascimento (dd/MM/yyyy)
    selectedSexoId: MutableState<Int?>, // NOVO: ID do Sexo
    selectedSexoName: MutableState<String>, // NOVO: Nome do Sexo
    // Passo 2 - Endereço & Senha
    cep: MutableState<String>,
    logradouro: MutableState<String>,
    numero: MutableState<String>,
    complemento: MutableState<String>,
    bairro: MutableState<String>,
    cidade: MutableState<String>,
    estado: MutableState<String>,
    senha: MutableState<String>,
    confirmarSenha: MutableState<String>,
    concordaTermos: MutableState<Boolean>,
    // Controles
    currentStep: MutableState<Int>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    usuarioService: UsuarioService,
    sexoService: SexoService,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // =================================================================
        // ITENS DO PASSO 1 (REGISTRO DO USUÁRIO)
        // =================================================================
        if (currentStep.value == 1) {
            item {
                RegistroOutlinedTextField(
                    value = nome.value,
                    onValueChange = { nome.value = it },
                    label = stringResource(R.string.label_user_name),
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.desc_icon_name),
                            tint = Color(0x9E000000))
                    },
                    readOnly = isLoading.value
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = stringResource(R.string.label_email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.desc_icon_email), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it.filter { c -> c.isDigit() }.take(11) }, // Limita a 11 dígitos
                    label = stringResource(R.string.label_phone),
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = stringResource(R.string.desc_icon_phone), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }
            item {
                // CPF
                CpfTextField(
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isLoading.value,
                    onValidationSuccess = { cleanCpf ->
                        cpf.value = cleanCpf
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                )
            }
            item {
                // Data de Nascimento (dd/MM/yyyy)
                RegistroOutlinedTextField(
                    value = dataNascimento.value,
                    onValueChange = { newText ->
                        // Lógica de máscara simples dd/MM/yyyy
                        var filteredText = newText.filter { it.isDigit() }.take(8) // 8 dígitos
                        if (filteredText.length > 4) {
                            filteredText = filteredText.substring(0, 4) + "/" + filteredText.substring(4)
                        }
                        if (filteredText.length > 2) {
                            filteredText = filteredText.substring(0, 2) + "/" + filteredText.substring(2)
                        }
                        dataNascimento.value = filteredText.take(10) // Limita o tamanho com a máscara
                    },
                    label = "Data de Nascimento (dd/MM/aaaa)",
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Data de Nascimento", tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                // Sexo (Selector customizado)
                SexoSelector(
                    selectedSexoId = selectedSexoId,
                    selectedSexoName = selectedSexoName,
                    isEnabled = !isLoading.value,
                    sexoService = sexoService,
                    scope = scope
                )
            }


            // Controles de Navegação do Passo 1
            item {
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.button_back),
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )

                    val isNextEnabled = !isLoading.value && isStep1Valid(nome.value, email.value, phone.value, cpf.value, dataNascimento.value, selectedSexoId.value)

                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isNextEnabled) {
                                    errorMessage.value = null
                                    currentStep.value = 2
                                } else {
                                    errorMessage.value = context.getString(R.string.error_fill_all_step_1)
                                }
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextEnabled) PrimaryColor else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = stringResource(R.string.button_next), tint = if (isNextEnabled) PrimaryColor else Color.LightGray)
                    }
                }
            }
        }

        // =================================================================
        // ITENS DO PASSO 2 (ENDEREÇO E SENHA)
        // =================================================================
        if (currentStep.value == 2) {
            // CEP (Você precisaria do componente CepTextField e ViaCepData definidos)
            // Para simplificar, vou usar um campo de texto comum para o CEP,
            // e os campos de endereço dependentes serão tratados como preenchidos manualmente
            item {
                RegistroOutlinedTextField(
                    value = cep.value,
                    onValueChange = { cep.value = it.filter { c -> c.isDigit() }.take(8) },
                    label = stringResource(R.string.label_cep),
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.label_cep), tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            val isAddressInputVisible = cep.value.length == 8 // Simula a busca do CEP

            if (isAddressInputVisible) {
                item {
                    RegistroOutlinedTextField(
                        value = logradouro.value,
                        onValueChange = { logradouro.value = it },
                        label = stringResource(R.string.label_street),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.label_street), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = numero.value,
                            onValueChange = { numero.value = it.filter { c -> c.isDigit() } },
                            label = stringResource(R.string.label_number_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        RegistroOutlinedTextField(
                            value = complemento.value,
                            onValueChange = { complemento.value = it },
                            label = stringResource(R.string.label_complement_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                    }
                }

                item {
                    RegistroOutlinedTextField(
                        value = bairro.value,
                        onValueChange = { bairro.value = it },
                        label = stringResource(R.string.label_neighborhood),
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = stringResource(R.string.label_neighborhood), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = cidade.value,
                            onValueChange = { cidade.value = it },
                            label = stringResource(R.string.label_city),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                        RegistroOutlinedTextField(
                            value = estado.value,
                            onValueChange = { estado.value = it.take(2) }, // Limita a 2 caracteres para UF
                            label = stringResource(R.string.label_state_uf),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value
                        )
                    }
                }
            }

            // Senhas
            item {
                RegistroOutlinedTextField(
                    value = senha.value,
                    onValueChange = { senha.value = it },
                    label = stringResource(R.string.label_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = confirmarSenha.value,
                    onValueChange = { confirmarSenha.value = it },
                    label = stringResource(R.string.label_confirm_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value
                )
            }

            // Termos de Serviço
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = concordaTermos.value,
                        onCheckedChange = { concordaTermos.value = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryColor),
                        enabled = !isLoading.value
                    )
                    Text(
                        text = stringResource(R.string.label_terms_agreement),
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = stringResource(R.string.label_terms_link),
                        color = PrimaryColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = !isLoading.value) { /* Ação para ver termos */ }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Controles de Navegação do Passo 2 (Cadastrar)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = 1
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.button_back),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }

                    val isSubmitEnabled = !isLoading.value && isStep2Valid(
                        logradouro.value, bairro.value, cidade.value, estado.value,
                        senha.value, confirmarSenha.value, concordaTermos.value
                    )

                    Row(
                        modifier = Modifier
                            .clickable(enabled = isSubmitEnabled) {
                                errorMessage.value = null

                                if (!isSubmitEnabled) {
                                    errorMessage.value = "Por favor, preencha todos os campos obrigatórios e aceite os termos."
                                    return@clickable
                                }

                                // 1. Conversão e validação final da data de nascimento
                                val dataNascLocalDate: LocalDate? = try {
                                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    LocalDate.parse(dataNascimento.value, formatter)
                                } catch (e: DateTimeParseException) {
                                    errorMessage.value = "Formato de data inválido. Use dd/MM/aaaa."
                                    return@clickable
                                }

                                // 2. Garantir que Sexo foi selecionado
                                val idSexoFinal = selectedSexoId.value
                                if (idSexoFinal == null || idSexoFinal <= 0) {
                                    errorMessage.value = "Selecione seu Gênero (Sexo)."
                                    return@clickable
                                }

                                isLoading.value = true
                                scope.launch {
                                    try {
                                        // MONTAGEM DA REQUISIÇÃO DO USUÁRIO
                                        val request = UsuarioRequest(
                                            nome = nome.value,
                                            foto_perfil = null,
                                            email = email.value,
                                            senha = senha.value,
                                            data_nascimento = dataNascLocalDate!!,
                                            cpf = cpf.value,
                                            id_sexo = idSexoFinal,
                                            sexo = selectedSexoName.value,
                                            id_tipo_nivel = 1, // Fixado como 1 (Padrão)
                                            nivel_usuario = null,
                                            cep = cep.value,
                                            logradouro = logradouro.value,
                                            numero = numero.value.takeIf { it.isNotBlank() } ?: "S/N",
                                            complemento = complemento.value.takeIf { it.isNotBlank() },
                                            bairro = bairro.value,
                                            cidade = cidade.value,
                                            estado = estado.value
                                        )

                                        val response: Response<UsuarioResponse> = usuarioService.criar(request)

                                        if (response.isSuccessful && response.body() != null) {
                                            // 1. Navegar para a tela de Home (Sucesso)
                                            navController?.navigate("tela_home") {
                                                popUpTo("tela_registro") { inclusive = true }
                                            }

                                        } else {
                                            val errorBody = response.errorBody()?.string() ?: response.message()
                                            errorMessage.value = context.getString(R.string.error_registration_failed) + "\nDetalhe: $errorBody"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage.value = context.getString(R.string.error_connection_failed) + "\nDetalhe: ${e.message}"
                                    } finally {
                                        isLoading.value = false
                                    }
                                }
                            },
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isLoading.value) {
                            CircularProgressIndicator(
                                color = PrimaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.button_submit_register),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSubmitEnabled) PrimaryColor else Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }
}
