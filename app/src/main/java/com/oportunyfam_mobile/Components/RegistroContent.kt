package com.oportunyfam_mobile.Components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile.R
import com.oportunyfam_mobile.Service.SexoService
import com.oportunyfam_mobile.Service.UsuarioService
import com.oportunyfam_mobile.model.Usuario
import com.oportunyfam_mobile.model.UsuarioRequest
import com.oportunyfam_mobile.model.UsuarioResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Constantes de controle de etapa
private const val STEP_DADOS_PESSOAIS = 1
private const val STEP_CONTATO_E_ENDERECO = 2
private const val STEP_SENHAS_E_TERMOS = 3
private const val TIPO_NIVEL_PADRAO = 1

/**
 * Valida a data de nascimento e formata para o padrão LocalDate (DD/MM/AAAA).
 * Garante que a data tem 10 caracteres e não está no futuro.
 */
fun formatarDataNascimentoParaLocalDate(dataNascimento: String): LocalDate? {
    if (dataNascimento.length != 10) return null
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(dataNascimento, inputFormatter)

        // Impede datas futuras
        if (date.isAfter(LocalDate.now())) {
            return null
        }
        return date
    } catch (e: DateTimeParseException) {
        null
    }
}

/**
 * Converte a string de data (DD/MM/AAAA) para o formato do backend (AAAA-MM-DD).
 * @param dataNascimento String no formato DD/MM/AAAA.
 * @return String no formato YYYY-MM-DD ou uma string vazia se inválida.
 */
fun convertDataParaBackendFormat(dataNascimento: String): String {
    // dataNascimento deve estar no formato "DD/MM/YYYY"
    if (dataNascimento.length != 10) return ""
    return try {
        val parts = dataNascimento.split("/")
        // parts[0] = DD, parts[1] = MM, parts[2] = YYYY
        // Backend espera: YYYY-MM-DD
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } catch (e: Exception) {
        ""
    }
}

/**
 * Aplica a máscara DD/MM/AAAA na string de data de nascimento.
 */
fun aplicarMascaraDataNascimento(newValue: String): String {
    val cleanValue = newValue.filter { it.isDigit() }.take(8)
    return when {
        cleanValue.length > 4 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2, 4)}/${cleanValue.substring(4)}"
        cleanValue.length > 2 -> "${cleanValue.substring(0, 2)}/${cleanValue.substring(2)}"
        else -> cleanValue
    }
}

/**
 * Componente principal para o fluxo de registro de Usuário/Responsável.
 */
@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Dados Pessoais
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    dataNascimento: MutableState<String>,
    cpf: MutableState<String>,
    idSexo: MutableState<Int?>,
    selectedSexoName: MutableState<String>,
    // Endereço
    cep: MutableState<String>,
    logradouro: MutableState<String>,
    numero: MutableState<String>,
    complemento: MutableState<String>,
    bairro: MutableState<String>,
    cidade: MutableState<String>,
    estado: MutableState<String>,
    // Senhas
    senha: MutableState<String>,
    confirmarSenha: MutableState<String>,
    concordaTermos: MutableState<Boolean>,
    // Controles
    currentStep: MutableState<Int>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    usuarioService: UsuarioService,
    sexoService: SexoService,
    scope: CoroutineScope,
    onAuthSuccess: (Usuario) -> Unit
) {
    val context = LocalContext.current

    // Estado local para a validação do CPF
    var isCpfInputValid by remember { mutableStateOf(false) }

    // Variáveis de validação
    val dataNascLocalDate = formatarDataNascimentoParaLocalDate(dataNascimento.value)
    val isBirthDateValid = dataNascLocalDate != null
    val isSexoSelected = idSexo.value != null

    // Validações de Endereço
    // Complemento é opcional, mas Logradouro, Bairro, Cidade, Estado e Número são obrigatórios.
    val isAddressValid = cep.value.length == 8 && logradouro.value.isNotBlank() && bairro.value.isNotBlank() && cidade.value.isNotBlank() && estado.value.isNotBlank() && numero.value.isNotBlank()

    // Etapa 1: Nome, Email, Sexo, CPF (TODOS OBRIGATÓRIOS)
    val isStep1Valid = nome.value.isNotBlank() && email.value.isNotBlank() && isCpfInputValid && isSexoSelected

    // Etapa 2: Telefone, Data de Nascimento, Endereço completo (TODOS OBRIGATÓRIOS)
    val isStep2Valid = phone.value.isNotBlank() && isBirthDateValid && isAddressValid

    // Etapa 3: Senhas e Termos
    val isPasswordMatch = senha.value.length >= 6 && senha.value == confirmarSenha.value
    val isStep3Valid = isPasswordMatch && concordaTermos.value

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // --- ETAPA 1: DADOS PESSOAIS ---
        if (currentStep.value == STEP_DADOS_PESSOAIS) {
            item {
                RegistroOutlinedTextField(
                    value = nome.value,
                    onValueChange = { nome.value = it },
                    label = stringResource(R.string.label_name),
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    supportingText = { Text(stringResource(R.string.label_name)) }
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = stringResource(R.string.label_email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    supportingText = { Text(stringResource(R.string.label_email)) }
                )
            }
            item {
                SexoSelector(
                    selectedSexoId = idSexo,
                    selectedSexoName = selectedSexoName,
                    isEnabled = !isLoading.value,
                    sexoService = sexoService,
                    scope = scope
                )
            }
            // *** CAMPO CPF REINTRODUZIDO E OBRIGATÓRIO PARA AVANÇAR ***
            item {
                Column {
                    CpfTextField(
                        readOnly = isLoading.value,
                        onValidationSuccess = { cleanCpf ->
                            cpf.value = cleanCpf
                            isCpfInputValid = true
                        }
                    )
                    if (cpf.value.length == 11 && !isCpfInputValid && !isLoading.value) {
                        Text(
                            text = "Aperte 'Done' no campo CPF para validar o número.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            item {
                // Controles de Navegação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNextEnabled = !isLoading.value && isStep1Valid

                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isStep1Valid) {
                                    errorMessage.value = null
                                    currentStep.value = STEP_CONTATO_E_ENDERECO
                                } else {
                                    errorMessage.value = context.getString(R.string.error_fill_all_personal_data)
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

        // --- ETAPA 2: CONTATO E ENDEREÇO COMPLETO ---
        if (currentStep.value == STEP_CONTATO_E_ENDERECO) {
            item {
                RegistroOutlinedTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it.filter { char -> char.isDigit() || char in "()- " } },
                    label = stringResource(R.string.label_phone),
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Color(0x9E000000)) },
                    readOnly = isLoading.value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    supportingText = { Text(stringResource(R.string.label_phone)) }
                )
            }
            item {
                Column {
                    RegistroOutlinedTextField(
                        value = dataNascimento.value,
                        // Este onValueChange usa a máscara. Se a digitação estiver ruim,
                        // é necessário implementar controle de cursor via TextFieldValue.
                        onValueChange = { newValue ->
                            dataNascimento.value = aplicarMascaraDataNascimento(newValue)
                        },
                        label = "Data de Nascimento (DD/MM/AAAA)",
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0x9E000000)) },
                        readOnly = isLoading.value,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {}
                    )
                    if (dataNascimento.value.length == 10 && !isBirthDateValid && !isLoading.value) {
                        Text(
                            text = "Data inválida (formato ou futura). Use DD/MM/AAAA.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            // --- CAMPOS DE ENDEREÇO (Movidos para a Etapa 2) ---
            item {
                // 1. CEP (COMPONENTE CUSTOMIZADO)
                CepTextField(
                    modifier = Modifier.fillMaxWidth(),
                    onValidationSuccess = { data: ViaCepData ->
                        // Atualiza os estados principais do formulário com os dados da API
                        cep.value = data.cep.replace("-", "") // Salva o CEP limpo
                        logradouro.value = data.logradouro
                        bairro.value = data.bairro
                        cidade.value = data.localidade
                        estado.value = data.uf
                        errorMessage.value = null
                    }
                )
            }

            // Gatilho visual de que o CEP já foi digitado e processado
            val isAddressInputVisible = cep.value.isNotBlank() && cep.value.length == 8

            if (isAddressInputVisible) {
                item {
                    // Logradouro (Rua, Avenida) - Preenchido automaticamente, mas editável
                    RegistroOutlinedTextField(
                        value = logradouro.value,
                        onValueChange = { logradouro.value = it },
                        label = stringResource(R.string.label_street),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.label_street), tint = Color(0x9E000000)) },
                        readOnly = isLoading.value,
                        supportingText = { Text(stringResource(R.string.label_street)) }
                    )
                }

                item {
                    // Número/Complemento (Número é OBRIGATÓRIO, Complemento Opcional)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = numero.value,
                            onValueChange = { numero.value = it },
                            label = stringResource(R.string.label_number), // Alterado para indicar que é obrigatório
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            supportingText = { Text(stringResource(R.string.label_number)) }
                        )
                        RegistroOutlinedTextField(
                            value = complemento.value,
                            onValueChange = { complemento.value = it },
                            label = stringResource(R.string.label_complement_optional),
                            modifier = Modifier.weight(1f),
                            readOnly = isLoading.value,
                            supportingText = { Text(stringResource(R.string.label_complement_optional)) }
                        )
                    }
                }

                item {
                    // Bairro - Preenchido automaticamente
                    RegistroOutlinedTextField(
                        value = bairro.value,
                        onValueChange = { bairro.value = it },
                        label = stringResource(R.string.label_neighborhood),
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = stringResource(R.string.label_neighborhood), tint = Color(0x9E000000)) },
                        readOnly = true, // Mantido como ReadOnly pois vem do CEP
                        supportingText = { Text(stringResource(R.string.label_neighborhood)) }
                    )
                }

                item {
                    // Cidade/Estado - Preenchido automaticamente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistroOutlinedTextField(
                            value = cidade.value,
                            onValueChange = { cidade.value = it },
                            label = stringResource(R.string.label_city),
                            modifier = Modifier.weight(1f),
                            readOnly = true, // Mantido como ReadOnly pois vem do CEP
                            supportingText = { Text(stringResource(R.string.label_city)) }
                        )
                        RegistroOutlinedTextField(
                            value = estado.value,
                            onValueChange = { estado.value = it },
                            label = stringResource(R.string.label_state_uf),
                            modifier = Modifier.weight(1f),
                            readOnly = true, // Mantido como ReadOnly pois vem do CEP
                            supportingText = { Text(stringResource(R.string.label_state_uf)) }
                        )
                    }
                }
            }


            item {
                // Controles de Navegação
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = STEP_DADOS_PESSOAIS
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

                    val isNextEnabled = !isLoading.value && isStep2Valid

                    // Botão Próximo
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isNextEnabled) {
                                if (isStep2Valid) {
                                    errorMessage.value = null
                                    currentStep.value = STEP_SENHAS_E_TERMOS
                                } else {
                                    errorMessage.value = "Preencha todos os campos obrigatórios (Telefone, Data, Endereço completo, incluindo o Número) e valide a data de nascimento."
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

        // --- ETAPA 3: SENHAS E TERMOS ---
        if (currentStep.value == STEP_SENHAS_E_TERMOS) {
            item {
                RegistroOutlinedTextField(
                    value = senha.value,
                    onValueChange = { senha.value = it },
                    label = stringResource(R.string.label_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value,
                    supportingText = {
                        if (senha.value.length > 0 && senha.value.length < 6) {
                            Text(
                                "A senha deve ter no mínimo 6 caracteres.",
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(stringResource(R.string.label_password))
                        }
                    }
                )
            }
            item {
                RegistroOutlinedTextField(
                    value = confirmarSenha.value,
                    onValueChange = { confirmarSenha.value = it },
                    label = stringResource(R.string.label_confirm_password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0x9E000000)) },
                    visualTransformation = PasswordVisualTransformation(),
                    readOnly = isLoading.value,
                    supportingText = {
                        if (confirmarSenha.value.isNotEmpty() && senha.value != confirmarSenha.value) {
                            Text("As senhas não coincidem.", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(stringResource(R.string.label_confirm_password))
                        }
                    }
                )
            }

            item {
                // Termos de Serviço
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable(enabled = !isLoading.value) {
                            concordaTermos.value = !concordaTermos.value
                        },
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
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(enabled = !isLoading.value) { /* Ação para ver termos */ }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                // Controles de Navegação (Submissão)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Voltar
                    Row(
                        modifier = Modifier.clickable(enabled = !isLoading.value) {
                            errorMessage.value = null
                            currentStep.value = STEP_CONTATO_E_ENDERECO
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

                    val isSubmitEnabled = !isLoading.value && isStep3Valid

                    // Botão Cadastrar (Final)
                    Row(
                        modifier = Modifier
                            .clickable(enabled = isSubmitEnabled) {
                                errorMessage.value = null

                                if (!isSubmitEnabled) {
                                    errorMessage.value = "Preencha e confirme as senhas (mín. 6 caracteres) e aceite os termos."
                                    return@clickable
                                }

                                // Lógica de Submissão Integrada
                                isLoading.value = true

                                // Converter a data para o formato YYYY-MM-DD exigido pelo backend
                                val dataNascBackend = convertDataParaBackendFormat(dataNascimento.value)

                                scope.launch {
                                    try {
                                        val request = UsuarioRequest(
                                            nome = nome.value,
                                            email = email.value,
                                            senha = senha.value,
                                            telefone = phone.value.filter { it.isDigit() }.takeIf { it.isNotBlank() },
                                            cpf = cpf.value,
                                            // *** CORREÇÃO: Passando a String no formato AAAA-MM-DD ***
                                            data_nascimento = dataNascBackend,

                                            id_sexo = idSexo.value!!,
                                            id_tipo_nivel = TIPO_NIVEL_PADRAO,
                                            logradouro = logradouro.value,
                                            // Numero é obrigatório
                                            numero = numero.value.takeIf { it.isNotBlank() },
                                            // Complemento é opcional
                                            complemento = complemento.value.takeIf { it.isNotBlank() },
                                            bairro = bairro.value,
                                            cidade = cidade.value,
                                            estado = estado.value,
                                            cep = cep.value,
                                            foto_perfil = ""
                                        )

                                        val response: Response<UsuarioResponse> = usuarioService.criar(request)

                                        if (response.isSuccessful && response.body() != null) {
                                            val usuarioResponse = response.body()!!
                                            usuarioResponse.usuario?.let { usuarioCriado ->
                                                onAuthSuccess(usuarioCriado) // Callback para a tela principal
                                            } ?: run {
                                                Log.e("REGISTRO_API", "Resposta de sucesso, mas objeto usuário nulo no body.")
                                                errorMessage.value = context.getString(R.string.error_registration_failed) + ". Falha ao processar os dados."
                                                isLoading.value = false
                                            }
                                        } else {
                                            val errorBody = response.errorBody()?.string()
                                            Log.e("REGISTRO_API", "Erro HTTP ${response.code()} (${response.message()}). Body: $errorBody")
                                            errorMessage.value = context.getString(R.string.error_registration_failed) + ". Por favor, verifique seus dados (CPF/Email duplicado) e tente novamente."
                                            isLoading.value = false
                                        }
                                    } catch (e: Exception) {
                                        Log.e("REGISTRO_API", "Erro de conexão/IO/Parsing: ${e.message}", e)
                                        errorMessage.value = context.getString(R.string.error_connection_failed) + ". Verifique sua conexão com a internet."
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

        item {
            // Exibir erro genérico
            if (!errorMessage.value.isNullOrBlank()) {
                Text(
                    text = errorMessage.value!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}